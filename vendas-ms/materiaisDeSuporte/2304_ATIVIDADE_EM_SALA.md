# Atividade: Observabilidade em Microsserviços Spring Boot

**Disciplina:** Arquitetura de Microsserviços  
**Tempo estimado:** 90 minutos  
**Pré-requisito:** projeto `entregas-ms` funcionando com ActiveMQ e MySQL

---

## Contexto

O microsserviço `entregas-ms` já processa pedidos via fila JMS e persiste entregas no banco de dados.
Nesta atividade você vai adicionar os **três pilares de observabilidade** ao serviço:

| Pilar | Ferramenta | O que faz |
|-------|-----------|-----------|
| Logs  | Loki + Grafana | Centraliza e visualiza logs estruturados |
| Métricas | Actuator + Prometheus | Expõe indicadores de saúde e performance |
| Traces | Micrometer Tracing + Zipkin | Rastreia uma requisição de ponta a ponta |

> **Dica geral:** sempre que uma instrução pedir que você "pesquise", o conteúdo da aula e a
> documentação oficial do Spring Boot (`https://docs.spring.io/spring-boot/reference/`) são o
> ponto de partida. Use também a busca do Micrometer Docs e exemplos no GitHub.

---

## Parte 1 — Infraestrutura com Docker Compose (Validar em Vendas-ms)

O arquivo `compose.yaml` atual sobe apenas o MySQL. Você vai adicionar **Loki** e **Grafana**.

### Passo 1.1 — Adicionar Loki ao Compose

Pesquise a imagem oficial do Loki no Docker Hub (`grafana/loki`) e adicione um serviço que:
- Use a versão `3.0.0`
- Exponha a porta `3100`
- Passe o argumento `-config.file=/etc/loki/local-config.yaml`

> Dica: o Loki já vem com uma configuração padrão embutida nessa imagem. Você não precisa
> montar nenhum arquivo extra para o ambiente de desenvolvimento.

### Passo 1.2 — Adicionar Grafana ao Compose

Pesquise a imagem `grafana/grafana` (versão `11.0.0`) e adicione um serviço que:
- Exponha a porta `3000`
- Habilite acesso anônimo como Admin (variáveis de ambiente `GF_AUTH_ANONYMOUS_ENABLED`
  e `GF_AUTH_ANONYMOUS_ORG_ROLE`)
- Declare `depends_on: loki` para garantir a ordem de inicialização

**Perguntas para reflexão:**
- Por que o `depends_on` não garante que o Loki esteja *pronto* para receber conexões, apenas que o container foi *iniciado*?
- Qual seria a solução para aguardar o healthcheck do Loki antes de subir o Grafana?

### Passo 1.3 — Subir o ambiente

```bash
docker compose up -d
```

Verifique que o Grafana está acessível em `http://localhost:3000`.

---

## Parte 2 — Dependências Maven

Abra o `pom.xml` e adicione as dependências abaixo. Para cada uma, **pesquise o propósito**
antes de copiar — entender o que cada artefato faz é parte da atividade.

### Passo 2.1 — Spring Boot Actuator

Pesquise a dependência `spring-boot-starter-actuator` e adicione-a ao `pom.xml`.

> O que é o Actuator? Quais endpoints ele expõe por padrão?

### Passo 2.2 — Micrometer + Prometheus

Adicione o artefato `micrometer-registry-prometheus` do grupo `io.micrometer`.

> Qual a diferença entre Micrometer e Prometheus? Qual o papel de cada um?

### Passo 2.3 — Rastreamento Distribuído (Tracing)

Adicione os dois artefatos responsáveis por enviar traces ao Zipkin:
- `micrometer-tracing-bridge-brave` (grupo `io.micrometer`)
- `zipkin-reporter-brave` (grupo `io.zipkin.reporter2`)

> Pesquise o que é o protocolo **B3 Propagation** e por que ele é necessário em
> comunicações assíncronas (JMS, Kafka).

### Passo 2.4 — Loki Logback Appender

Adicione `loki-logback-appender` do grupo `com.github.loki4j` (versão `1.5.2`).

> Este appender envia logs diretamente do Logback para o Loki, sem precisar de um agente
> intermediário como o Promtail. Compare as duas abordagens.

---

## Parte 3 — Configuração do Logback

Crie o arquivo `src/main/resources/logback-spring.xml`.

### Passo 3.1 — Console Appender com traceId

Configure um `ConsoleAppender` com um pattern que inclua `%X{traceId}` e `%X{spanId}`.

> Por que incluir o `traceId` no log? Como isso ajuda na depuração em produção?

### Passo 3.2 — Loki Appender

Configure um `Loki4jAppender` apontando para `http://localhost:3100/loki/api/v1/push`.

Defina as **labels** do Loki com pelo menos:
- `app` = nome do serviço
- `level` = nível do log

> Por que manter a cardinalidade de labels baixa é uma boa prática no Loki?

### Passo 3.3 — Root Logger

Configure o root logger no nível `INFO` referenciando os dois appenders criados.

---

## Parte 4 — application.properties

### Passo 4.1 — Expor endpoints do Actuator

Adicione as propriedades para:
- Expor **todos** os endpoints via HTTP
- Mostrar os detalhes de health (`show-details=always`)
- Adicionar a tag `application` em todas as métricas com o valor `${spring.application.name}`

> Pesquise: por que expor todos os endpoints (`*`) é adequado para desenvolvimento mas
> problemático em produção?

### Passo 4.2 — Configurar o Zipkin

Adicione as propriedades:
- Probabilidade de amostragem (`sampling.probability`) = `1.0`
- URL do endpoint do Zipkin: `http://localhost:9411/api/v2/spans`

> O que significa `sampling.probability=1.0`? Qual valor seria adequado para produção?

---

## Parte 5 — SecurityConfig

O Spring Security, quando detecta o Actuator no classpath, bloqueia os endpoints `/actuator/**`
por padrão. Crie a classe `SecurityConfig` para liberar o acesso.

### Passo 5.1 — Criar a classe

Crie `src/main/java/br/com/fiap/entregasms/configurations/SecurityConfig.java` como um
`@Configuration` que declara um `@Bean` do tipo `SecurityFilterChain`.

### Passo 5.2 — Permitir endpoints do Actuator

Pesquise a classe `EndpointRequest` do pacote
`org.springframework.boot.actuate.autoconfigure.security.servlet` e use-a para liberar
**todos** os endpoints do Actuator sem autenticação.

Mantenha o OAuth2 Login (GitHub) como mecanismo de autenticação para as demais rotas.

> Por que usar `EndpointRequest.toAnyEndpoint()` é preferível a
> `.requestMatchers("/actuator/**")`?

---

## Parte 6 — Propagação de Trace no Consumer JMS

Esta é a parte mais complexa. O `PedidoEntregaConsumer` precisa **continuar o trace** que
foi iniciado no `vendas-ms` quando a mensagem foi publicada na fila.

### Passo 6.1 — Injetar as dependências

Adicione ao construtor do consumer:
- `Tracer` (interface do Micrometer Tracing)
- `Propagator` (interface do Micrometer Tracing)
- `JmsTemplate` (para publicar a resposta)

### Passo 6.2 — Alterar a assinatura do método

Mude o parâmetro de `String message` para `jakarta.jms.Message jmsMessage`.

> Por que precisamos do objeto `Message` em vez de `String`? O que ele carrega a mais?

### Passo 6.3 — Extrair o contexto de trace

Use `propagator.extract(...)` para ler os headers B3 da mensagem JMS. Atenção: o JMS
não aceita o caractere `-` em nomes de propriedade, então pesquise como contornar isso
(dica: o `vendas-ms` substitui `-` por `_` ao gravar os headers).

### Passo 6.4 — Criar e fechar o Span

Envolva o processamento com `tracer.withSpan(span)` dentro de um bloco `try-with-resources`
e garanta que `span.end()` seja chamado no bloco `finally`.

### Passo 6.5 — Adicionar logs estruturados

Declare um `Logger` estático e adicione logs para:
- Mensagem recebida (com `traceId`)
- Entrega criada (com `pedidoId`)
- Evento publicado (com `entregaId` e `status`)

### Passo 6.6 — Publicar evento de retorno

Após salvar a entrega, serialize um objeto com `id` e `status` e publique na fila
`entrega.queue` usando `jmsTemplate.convertAndSend(...)`.

---

## Parte 7 — Verificação

Com tudo configurado, suba a aplicação e valide:

1. **Actuator:** acesse `http://localhost:8182/actuator/health` — deve retornar `UP`
2. **Prometheus:** acesse `http://localhost:8182/actuator/prometheus` — deve listar métricas
3. **Logs no Grafana:**
    - Acesse `http://localhost:3000`
    - Navegue até **Explore** → selecione Loki como datasource
    - Execute a query `{app="entregas-ms"}` e confirme que os logs aparecem
4. **Trace:** envie uma mensagem para a fila `pedido.queue` e procure o trace no Zipkin
   (`http://localhost:9411`)

---

