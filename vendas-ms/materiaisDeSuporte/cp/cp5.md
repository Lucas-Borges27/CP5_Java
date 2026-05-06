# CP5 — Java Advanced
**Prazo de entrega:** até o dia 09/05 às 23:55
**Pontuação total:** 10 pontos
**Entrega:** Repositório GitHub com link enviado pelo portal

## Aviso sobre Uso de Inteligência Artificial

O uso de ferramentas de IA é permitido como apoio ao estudo, mas o uso **indiscriminado** poderá gerar **desconto de até 40% da nota total**. Considera-se uso indiscriminado a entrega de implementações que: (a) não demonstrem familiaridade com as decisões de projeto tomadas ao longo das aulas; (b) reproduzam padrões genéricos que contradizem correções e escolhas explicitamente discutidas em sala; (c) ignorem os contratos e restrições descritos neste enunciado; ou (d) apresentem comportamentos que indicam que o aluno não compreendeu o código entregue.


## Contexto

O `vendas-ms` gerencia clientes e pedidos. Cada pedido possui uma descrição livre informada pelo usuário — mas, em um sistema real, o que faz sentido comercializar é um **produto do catálogo**. O `produtos-ms`, criado no CP4, é justamente esse catálogo: o repositório central de itens disponíveis para venda.

Hoje, os dois serviços não se comunicam. O `vendas-ms` não sabe quais produtos estão disponíveis no catálogo e o `produtos-ms` não notifica ninguém quando seu catálogo muda.

Este trabalho tem dois objetivos:

**1. Mensageria:** quando um produto é salvo no `produtos-ms`, esse serviço deve publicar um evento assíncrono para que outros serviços interessados sejam notificados. O `vendas-ms` será o consumidor desse evento — ao recebê-lo, registrará a informação em log. Essa integração segue o mesmo Outbox Pattern já implementado no `vendas-ms`, e é o ponto de partida para que, futuramente, o `vendas-ms` passe a referenciar produtos reais do catálogo ao invés de descrições livres.

**2. Observabilidade:** os dois serviços devem ser instrumentados de forma que seja possível acompanhar o que acontece neles — quais operações estão sendo executadas, quanto tempo levam e quais erros ocorrem. A observabilidade é implementada em três camadas: **logs** com as boas práticas de estruturação, **métricas** consultáveis via Actuator e **rastreabilidade** com propagação de trace entre os serviços.

A entrega exige como evidência um **print do Zipkin mostrando um trace completo**, que abranja desde a requisição HTTP de salvamento no `produtos-ms` até o consumo da mensagem no `vendas-ms`, com os dois serviços conectados pelo mesmo `traceId`.


## Parte 1 — Observabilidade

### O que é observabilidade neste contexto

Observabilidade é a capacidade de responder perguntas sobre o comportamento interno de um sistema a partir de suas saídas externas. No contexto deste trabalho, as saídas são três: logs, métricas e traces.

Não é exigida nenhuma ferramenta externa de visualização de logs ou métricas neste trabalho. Os logs devem ser consultáveis no console da aplicação e as métricas, no endpoint do Actuator. O Zipkin é a única ferramenta de visualização exigida, e já faz parte da infraestrutura do `vendas-ms`.

### Logs

O `produtos-ms` deve registrar em log as operações relevantes do domínio. As mensagens de log devem seguir as boas práticas discutidas em aula:

- Utilizar os níveis de severidade adequados: `DEBUG` para detalhes de diagnóstico, `INFO` para operações concluídas com sucesso, `WARN` para situações inesperadas mas recuperáveis e `ERROR` para falhas
- Incluir nos parâmetros da mensagem os identificadores que permitam rastrear o contexto da operação (ex: código do produto)
- Não interpolar strings com concatenação — usar os placeholders `{}` do SLF4J
- O `traceId` e o `spanId` devem aparecer automaticamente em cada linha de log, via MDC, quando o rastreamento estiver configurado

O `vendas-ms` já segue essas práticas e pode ser usado como referência.

### Métricas

O `produtos-ms` deve expor o endpoint `/actuator/metrics`. Além das métricas padrão do Spring Boot, o serviço deve registrar **ao menos uma métrica customizada** com Micrometer que reflita uma operação relevante do domínio — por exemplo, um contador incrementado a cada vez que um produto é salvo.

A métrica customizada deve estar visível ao acessar `/actuator/metrics/{nome-da-metrica}` no browser.

### Rastreamento distribuído

O `produtos-ms` deve exportar spans para o Zipkin com taxa de amostragem de 100% no ambiente de desenvolvimento. A configuração deve seguir o mesmo padrão de propriedades presente no `vendas-ms`. O Zipkin já está disponível na infraestrutura do `docker-compose` do projeto — nenhuma nova instância precisa ser criada.

O `traceId` gerado em uma requisição HTTP ao `produtos-ms` deve se propagar para o consumidor no `vendas-ms` através da mensagem JMS, permitindo visualizar o trace completo no Zipkin como uma única cadeia de spans.

### Configuração do Actuator no produtos-ms

O endpoint `/actuator/health` deve estar acessível sem autenticação. Os demais endpoints relevantes — `metrics`, `info` e `httptrace` (ou `httpexchanges`, dependendo da versão) — devem estar expostos. A configuração de segurança do `produtos-ms` deve permitir o acesso a esses endpoints da mesma forma que foi feito no `vendas-ms`.


## Parte 2 — Outbox Pattern no produtos-ms

### Contexto do padrão

O Outbox Pattern resolve o problema de garantir atomicidade entre a persistência de um registro de domínio e a publicação de um evento em um broker de mensagens. A alternativa ingênua — salvar no banco e depois publicar na fila — não garante consistência: o processo pode falhar entre os dois passos e o evento se perder silenciosamente.

A solução, discutida em aula e já implementada no `vendas-ms`, consiste em salvar o evento de saída na mesma transação do registro principal. Um job periódico lê os eventos pendentes e os publica no broker, marcando-os como processados.

### Entidade OutboxEvent

O `produtos-ms` deve ter sua própria entidade `OutboxEvent`, com a mesma estrutura e estratégia de geração de identificador adotadas no `vendas-ms`. O script de migration correspondente deve ser criado com numeração sequencial, sem dependência de criação automática de schema.

### Job de publicação

O job responsável pela publicação periódica dos eventos deve seguir o mesmo mecanismo de agendamento, lógica de leitura, publicação e atualização de status presente no `vendas-ms`.

### Propagação do contexto de trace

A publicação da mensagem JMS deve injetar o contexto de rastreamento nas propriedades da mensagem, usando o mesmo mecanismo de propagação de headers B3 utilizado no `vendas-ms`. Sem essa injeção, o Zipkin não consegue ligar o span do produtor ao span do consumidor — o trace completo não existirá.

### Integração com o salvamento de produto

Ao salvar um produto — seja criação ou edição — o serviço deve criar um `OutboxEvent` na mesma transação, com destino `produto.queue`. O payload deve conter ao menos o código identificador do produto. A criação do evento deve ocorrer na camada de serviço.


## Parte 3 — Consumidor no vendas-ms

### Listener JMS

O `vendas-ms` deve ser estendido com um consumidor que ouça a fila `produto.queue`. O consumidor representa o primeiro passo da integração entre os dois domínios: o serviço de vendas toma ciência de que o catálogo foi atualizado.

Ao receber uma mensagem, o consumidor deve:

1. **Restaurar o contexto de rastreamento** a partir das propriedades da mensagem JMS — extraindo os headers B3 da mensagem da mesma forma que os demais consumidores do `vendas-ms` fazem com a `entrega.queue`
2. **Registrar o recebimento** em log no nível `INFO`, incluindo o código do produto recebido
3. **Encerrar o span** ao fim do processamento

O consumidor não precisa persistir nenhuma informação neste trabalho.


## Parte 4 — Evidência de Rastreabilidade

### Requisito obrigatório

A entrega deve incluir **pelo menos um print** do Zipkin demonstrando um trace completo que abranja:

- Um span iniciado pela requisição HTTP de salvamento de produto no `produtos-ms`
- O span do job de publicação do `produtos-ms`
- O span do consumidor no `vendas-ms`

Os três spans devem compartilhar o mesmo `traceId`, confirmando que o rastreamento distribuído está corretamente propagado através da mensagem JMS.

O print deve ser salvo no repositório, em um diretório `evidencias/`, e referenciado no `README.md` com uma breve descrição do fluxo demonstrado.


## Critérios de Avaliação e Distribuição de Pontos

### 1. Logs (1,5 ponto)

| Critério | Pontos |
|---|--------|
| Uso correto dos níveis de severidade nas operações do domínio do `produtos-ms` | 0,5    |
| Mensagens de log com placeholders SLF4J e identificadores de contexto relevantes | 0,5    |
| `traceId` e `spanId` presentes nas linhas de log via MDC quando o rastreamento está ativo | 0,5    |

### 2. Métricas (1,0 ponto)

| Critério | Pontos |
|---|--------|
| Endpoint `/actuator/metrics` acessível no `produtos-ms` | 0,5    |
| Ao menos uma métrica customizada com Micrometer registrando uma operação do domínio, visível via Actuator | 0,5    |

### 3. Rastreamento distribuído (1,5 ponto)

| Critério | Pontos |
|---|--------|
| Dependências de rastreamento configuradas corretamente no `produtos-ms` | 0,5    |
| Taxa de amostragem de 100% e exportação para o Zipkin da infraestrutura existente | 0,5    |
| `traceId` visível nos logs do `produtos-ms` em requisições instrumentadas | 0,5    |

### 4. Outbox Pattern — Infraestrutura e Publicação (3,0 pontos)

| Critério | Pontos |
|---|--------|
| Entidade `OutboxEvent` criada com estrutura equivalente à do `vendas-ms` | 0,5    |
| Script de migration com numeração sequencial e sem dependência de criação automática de schema | 0,5    |
| Job de publicação funcionando corretamente (polling, publicação e atualização de status) | 0,75   |
| Injeção do contexto de trace nas propriedades da mensagem JMS (B3 headers) | 0,5    |
| Criação do `OutboxEvent` na mesma transação do salvamento do produto, na camada de serviço | 0,75   |

### 5. Consumidor no vendas-ms (1,5 ponto)

| Critério | Pontos |
|---|--------|
| Listener JMS configurado para a fila `produto.queue` | 0,5    |
| Restauração do contexto de trace a partir das propriedades da mensagem JMS | 0,5    |
| Log no nível `INFO` registrando o recebimento com o código do produto | 0,5    |

### 6. Evidência de Rastreabilidade (1,5 ponto)

| Critério | Pontos |
|---|--------|
| Print do Zipkin com trace completo conectando spans do `produtos-ms` e do `vendas-ms` sob o mesmo `traceId` | 1,0    |
| Diretório `evidencias/` no repositório com o print referenciado e descrito no `README.md` | 0,5    |


## Restrições e Observações

- O `produtos-ms` deve ter sua própria porta configurada para não conflitar com o `vendas-ms`
- O padrão de implementação do Outbox Pattern no `produtos-ms` deve ser derivado do que existe no `vendas-ms` — adaptações são permitidas, mas a estrutura deve ser reconhecível
- O banco de dados deve subir com `docker compose up -d` e as migrations devem ser aplicadas automaticamente na inicialização
- Credenciais de banco de dados e de outros serviços não devem ser commitadas — use variáveis de ambiente ou arquivos locais ignorados pelo `.gitignore`
- A integração entre `Produto` e `Pedido` continua **não obrigatória**
- O print de evidência deve ser obtido com os dois serviços em execução simultânea — um print que mostre spans de apenas um serviço não demonstra o trace completo
