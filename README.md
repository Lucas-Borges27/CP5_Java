# CP5 - Java Advanced - Observabilidade e Mensageria

## Integrantes do Grupo
- Lucas Borges De Souza - 560027
- Pedro Henrique da Silva - 560393  
- Bruno Carlos Soares - 559250

## Descrição do Projeto

Este projeto implementa a integração entre dois microserviços (`produtos-ms` e `vendas-ms`) através de **mensageria assíncrona** utilizando o **Outbox Pattern** e implementa **observabilidade completa** em ambos os serviços.

### Objetivos Alcançados

1. **Mensageria com Outbox Pattern**: Quando um produto é salvo no `produtos-ms`, o serviço publica um evento assíncrono na fila `produto.queue`. O `vendas-ms` consome esse evento e registra em log.

2. **Observabilidade Completa**:
   - **Logs estruturados** com SLF4J e MDC incluindo `traceId` e `spanId`
   - **Métricas customizadas** com Micrometer (contador de produtos salvos)
   - **Rastreamento distribuído** com Zipkin e propagação de contexto B3 entre serviços

## Arquitetura

```
┌─────────────────┐         ┌──────────────┐         ┌─────────────────┐
│  produtos-ms    │         │  ActiveMQ    │         │   vendas-ms     │
│  (porta 8082)   │────────▶│              │────────▶│  (porta 8080)   │
│                 │         │ produto.queue│         │                 │
│ - Outbox Pattern│         └──────────────┘         │ - Consumer JMS  │
│ - Job Publisher │                                  │ - Trace Restore │
└─────────────────┘                                  └─────────────────┘
         │                                                    │
         │                                                    │
         └────────────────────┬───────────────────────────────┘
                              │
                              ▼
                      ┌──────────────┐
                      │    Zipkin    │
                      │ (porta 9411) │
                      │ Trace Viewer │
                      └──────────────┘
```

## Tecnologias Utilizadas

- **Spring Boot 3.4.4**
- **Java 17**
- **MySQL 8.0** (bancos separados)
- **ActiveMQ** (mensageria JMS)
- **Zipkin** (rastreamento distribuído)
- **Micrometer** (métricas e tracing)
- **Flyway** (migrations de banco)
- **Logback** (logs estruturados)

## Estrutura do Projeto

```
cp5/
├── produtos-ms/              # Microserviço de produtos
│   ├── src/main/java/
│   │   └── br/com/fiap/produtosms/
│   │       ├── entities/
│   │       │   ├── Produto.java
│   │       │   └── OutboxEvent.java
│   │       ├── repositories/
│   │       │   ├── ProdutoRepository.java
│   │       │   └── OutboxEventRepository.java
│   │       ├── service/
│   │       │   ├── ProdutoService.java
│   │       │   ├── ProdutoServiceImpl.java (com logs e métricas)
│   │       │   ├── OutBoxService.java
│   │       │   └── OutBoxServiceImpl.java
│   │       ├── jobs/
│   │       │   └── OutBoxJob.java (publicação com propagação B3)
│   │       └── controller/
│   │           └── ProdutoController.java
│   └── src/main/resources/
│       ├── application.properties (porta 8082, Zipkin, Actuator)
│       ├── logback-spring.xml (traceId/spanId)
│       └── db/migration/
│           └── V6__create_outbox_event.sql
│
├── vendas-ms/                # Microserviço de vendas
│   └── src/main/java/
│       └── br/com/fiap/vendasms/
│           └── external_interface/jms/
│               └── ProdutoConsumer.java (restauração de trace B3)
│
└── evidencias/               # Prints do Zipkin
    └── trace-completo-zipkin.png
```

## Funcionalidades Implementadas

### produtos-ms

#### 1. Observabilidade
- ✅ Logs estruturados com níveis adequados (DEBUG, INFO, WARN, ERROR)
- ✅ Placeholders `{}` do SLF4J (sem concatenação de strings)
- ✅ `traceId` e `spanId` automáticos via MDC
- ✅ Endpoint `/actuator/metrics` acessível
- ✅ Métrica customizada: `produtos.salvos.total` (contador)
- ✅ Exportação de spans para Zipkin (taxa 100%)
- ✅ Configuração de segurança permitindo acesso aos endpoints do Actuator

#### 2. Outbox Pattern
- ✅ Entidade `OutboxEvent` com estrutura idêntica ao vendas-ms
- ✅ Migration `V6__create_outbox_event.sql` com numeração sequencial
- ✅ Repository com query para buscar eventos pendentes
- ✅ Service `OutBoxService` com interface e implementação
- ✅ Job periódico (`@Scheduled`) para publicação de eventos
- ✅ Injeção de contexto B3 nas propriedades da mensagem JMS
- ✅ Criação de `OutboxEvent` na mesma transação do salvamento do produto

### vendas-ms

#### 3. Consumidor JMS
- ✅ Listener para a fila `produto.queue`
- ✅ Restauração do contexto de trace a partir dos headers B3
- ✅ Log estruturado no nível INFO com código do produto
- ✅ Encerramento correto do span

## Como Executar

### Pré-requisitos
- Java 17+ instalado
- Maven instalado
- Docker e Docker Compose instalados

### Passo 1: Subir a infraestrutura

```bash
# No diretório vendas-ms
cd vendas-ms
podman compose up -d
# ou: docker compose up -d (se usar Docker)
```

Isso iniciará:
- MySQL (vendas-ms na porta 3306)
- ActiveMQ (porta 61616 para JMS, 8161 para console web)
- Zipkin (porta 9411)
- Prometheus, Grafana, Loki (observabilidade adicional)

### Passo 2: Subir o banco do produtos-ms

```bash
# No diretório produtos-ms
cd produtos-ms
podman compose up -d
# ou: docker compose up -d (se usar Docker)
```

Isso iniciará:
- MySQL (produtos-ms na porta 3307)

### Passo 3: Executar o produtos-ms

```bash
cd produtos-ms
./mvnw spring-boot:run
```

O serviço estará disponível em: http://localhost:8082

### Passo 4: Executar o vendas-ms

```bash
cd vendas-ms
./mvnw spring-boot:run
```

O serviço estará disponível em: http://localhost:8080

### Passo 5: Testar o fluxo completo

1. Acesse o produtos-ms: http://localhost:8082
2. Faça login com GitHub OAuth
3. Crie ou edite um produto
4. Observe os logs do produtos-ms mostrando:
   - Salvamento do produto com `traceId`
   - Criação do evento outbox
   - Publicação do evento pelo job (a cada 10 segundos)
5. Observe os logs do vendas-ms mostrando:
   - Recebimento da mensagem com o mesmo `traceId`
   - Código do produto recebido
6. Acesse o Zipkin: http://localhost:9411
7. Busque pelo `traceId` e visualize o trace completo

### Passo 6: Verificar métricas

Acesse: http://localhost:8082/actuator/metrics/produtos.salvos.total

Você verá a métrica customizada com o total de produtos salvos.

## Endpoints Importantes

### produtos-ms (porta 8082)
- `/` - Home
- `/produtos` - Listagem de produtos
- `/produtos/{codigo}` - Detalhes/edição de produto
- `/actuator/health` - Health check
- `/actuator/metrics` - Métricas disponíveis
- `/actuator/metrics/produtos.salvos.total` - Métrica customizada

### vendas-ms (porta 8080)
- `/` - Home
- `/clientes` - Gestão de clientes
- `/pedidos` - Gestão de pedidos
- `/actuator/health` - Health check
- `/actuator/metrics` - Métricas disponíveis

### Infraestrutura
- **ActiveMQ Console**: http://localhost:8161 (admin/admin)
- **Zipkin**: http://localhost:9411
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090

## Evidências de Rastreabilidade

O diretório `evidencias/` contém prints do Zipkin demonstrando:

1. **Trace completo** conectando:
   - Span da requisição HTTP no produtos-ms
   - Span do job de publicação no produtos-ms
   - Span do consumidor no vendas-ms
   - Todos compartilhando o mesmo `traceId`

### Fluxo Demonstrado no Trace

```
1. HTTP POST /produtos/{codigo} (produtos-ms)
   └─ traceId: abc123...
      └─ Salvamento do produto
      └─ Criação do OutboxEvent

2. Job OutBoxJob.publish() (produtos-ms)
   └─ traceId: abc123... (mesmo trace!)
      └─ Publicação na fila produto.queue
      └─ Injeção de headers B3

3. JMS Consumer ProdutoConsumer.consume() (vendas-ms)
   └─ traceId: abc123... (mesmo trace!)
      └─ Restauração do contexto B3
      └─ Log do produto recebido
```

\

## Observações Importantes

- O `produtos-ms` usa porta **8082** para não conflitar com o vendas-ms (8080)
- Ambos os serviços compartilham a mesma instância de ActiveMQ e Zipkin
- As credenciais OAuth do GitHub devem ser configuradas via variáveis de ambiente
- O padrão Outbox garante atomicidade entre persistência e publicação de eventos
- A propagação B3 permite rastreamento distribuído completo através da mensageria

## Troubleshooting

### Problema: Serviços não conectam ao banco
**Solução**: Verifique se os containers Docker estão rodando com `docker ps`

### Problema: Mensagens não aparecem no vendas-ms
**Solução**: 
1. Verifique se o ActiveMQ está rodando
2. Acesse o console do ActiveMQ (http://localhost:8161)
3. Verifique se a fila `produto.queue` existe e tem mensagens

### Problema: Trace não aparece no Zipkin
**Solução**:
1. Verifique se o Zipkin está rodando (http://localhost:9411)
2. Confirme que `management.tracing.sampling.probability=1.0` está configurado
3. Aguarde alguns segundos para os spans serem enviados

### Problema: Métrica customizada não aparece
**Solução**: Salve pelo menos um produto para incrementar o contador

## Conclusão

Este projeto demonstra a implementação completa de:
- Comunicação assíncrona entre microserviços com garantia de entrega (Outbox Pattern)
- Observabilidade em três pilares (logs, métricas, traces)
- Rastreamento distribuído com propagação de contexto através de mensageria
- Boas práticas de logging estruturado
- Métricas customizadas de negócio

A integração entre `produtos-ms` e `vendas-ms` está pronta para evoluir, permitindo que futuramente os pedidos referenciem produtos reais do catálogo ao invés de descrições livres.