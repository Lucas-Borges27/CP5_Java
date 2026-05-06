# CP4 — Java Advanced
**Prazo de entrega:** até o dia 28/03
**Pontuação total:** 10 pontos
**Entrega:** Repositório GitHub com link enviado pelo portal

## Aviso sobre Uso de Inteligência Artificial

O uso de ferramentas de IA é permitido como apoio ao estudo, mas o uso **indiscriminado** poderá gerar **desconto de até 40% da nota total**. Considera-se uso indiscriminado a entrega de implementações que: (a) não demonstrem familiaridade com as decisões de projeto tomadas ao longo das aulas; (b) reproduzam padrões genéricos que contradizem correções e escolhas explicitamente discutidas em sala; (c) ignorem os contratos e restrições descritos neste enunciado; ou (d) apresentem comportamentos que indicam que o aluno não compreendeu o código entregue.


## Contexto

O microserviço `vendas-ms` evoluiu ao longo das aulas e hoje conta com gestão de clientes, pedidos, autenticação via GitHub e controle de acesso por perfil. O próximo passo natural para esse domínio é a gestão do catálogo de produtos que podem ser referenciados em uma venda.

Seu trabalho consiste em **criar o `produtos-ms`**, um microsserviço de gestão de **Produtos**, respeitando integralmente os padrões e decisões arquiteturais adotados ao longo do curso.

## Descrição do Microsserviço de Produtos

### O que é um Produto neste contexto

Um produto representa um item que pode ser comercializado. Para fins deste sistema, um produto possui: um código identificador único gerado automaticamente (do mesmo tipo e estratégia de geração utilizada para identificar pedidos no projeto atual), um nome, uma descrição textual, um preço unitário representado como número decimal, e uma categoria textual livre.

O produto existe de forma independente de clientes e pedidos — ele é uma entidade do domínio e ciclo de vida próprio.

### Comportamento esperado do módulo

**Listagem de produtos**

A aplicação deve oferecer uma rota que exibe todos os produtos cadastrados em uma tabela. A tabela apresenta o código, nome, categoria e preço de cada produto. A partir dessa listagem, o usuário pode iniciar o cadastro de um novo produto ou acessar o detalhamento de um existente.

Quando não houver nenhum produto cadastrado, a tabela deve ser substituída por uma mensagem informativa. Essa lógica de exibição condicional deve seguir o mesmo mecanismo que foi utilizado para tratar a lista vazia de pedidos nas views do projeto.

**Cadastro e edição**

A rota de detalhamento de um produto, quando recebe um código que não existe no banco de dados, não deve exibir um erro para o usuário. Em vez disso, deve exibir um formulário em branco pronto para cadastro, com o campo de código já preenchido — exatamente o mesmo comportamento implementado no fluxo de busca e cadastro de clientes. O formulário deve ser idêntico tanto para criação quanto para edição.

O formulário exibe todos os campos do produto e permite sua alteração. Ao salvar, o sistema deve persistir as informações e redirecionar o usuário de forma a evitar que o recarregamento da página submeta o formulário novamente — padrão que foi discutido em sala e aplicado em outros fluxos do projeto.

**Exclusão**

A listagem deve oferecer a opção de excluir um produto. A exclusão deve ser confirmada pelo usuário antes de ser efetivada. O mecanismo de confirmação deve ser feito na camada do frontend, sem criar uma rota específica para isso.

### Contrato do DTO de Produto

O DTO de produto deve ser implementado como um tipo imutável, seguindo o mesmo padrão de imutabilidade adotado para os objetos de transferência de dados existentes no projeto. Ele deve oferecer:

- Um método estático que converte uma entidade em DTO
- Um método estático que converte uma lista de entidades em lista de DTOs
- Um método que converte o DTO de volta para entidade
- Um método estático que cria uma representação "vazia" do produto a partir apenas de seu código — para ser usada quando o produto ainda não existe no banco

O DTO não deve conter lógica de negócio. Toda validação e regra de domínio pertence à camada de serviço.

### Contrato da camada de serviço

A interface de serviço deve expor no mínimo as seguintes operações:

- Buscar um produto pelo seu código identificador — deve lançar uma exceção do tipo `NoSuchElementException` quando o produto não for encontrado
- Listar todos os produtos cadastrados
- Salvar ou atualizar um produto (operação unificada)
- Excluir um produto pelo seu código identificador

A implementação do serviço deve depender da abstração (interface), não da classe concreta. Esse princípio foi discutido em sala como parte dos fundamentos de bom design de software orientado a objetos.

### Infraestrutura de banco de dados

O esquema da tabela de produtos deve ser criado exclusivamente via o mecanismo de versionamento de banco de dados adotado pelo projeto a partir da aula que tratou da migração do gerenciamento automático de schema para um controle explícito e rastreável. Não deve haver nenhuma configuração que permita ao framework criar ou alterar a tabela de produtos automaticamente.

O script de criação deve seguir a numeração sequencial dos scripts já existentes no projeto. O tipo de dado utilizado para o campo identificador do produto deve ser o mesmo tipo que foi escolhido para o identificador da tabela de pedidos — essa escolha foi explicada em sala e documentada no material de suporte.

Se o projeto ainda estiver com a configuração que delega a criação de schema ao framework, essa configuração também deve ser corrigida como parte do trabalho.

### Segurança e controle de acesso

A rota de listagem de produtos é acessível a qualquer usuário autenticado.

As rotas de cadastro, edição e exclusão de produtos devem ser protegidas por uma role específica. Essa role deve seguir exatamente a mesma convenção de nomenclatura que foi adotada para a role que protege a área de pedidos. O acesso deve ser gerenciado diretamente no banco de dados, da mesma forma demonstrada em sala — não há interface administrativa para isso.

A configuração de segurança deve ser atualizada para incluir a nova restrição de rota. A ordem das regras de autorização na configuração de segurança é relevante e deve ser respeitada conforme o princípio discutido em sala.

Usuários que tentarem acessar uma rota protegida sem a devida role devem ser redirecionados para a página de acesso negado que já existe no projeto.

### Interface visual

Todas as views do módulo de produtos devem:

- Incluir a declaração necessária para que o motor de templates processe os atributos de template corretamente — a ausência dessa declaração foi identificada como causa de bug em um dos templates durante as aulas
- Utilizar o mecanismo de reutilização de templates para a barra de navegação, garantindo que a troca de idioma e o botão de logout funcionem na mesma página
- Herdar o comportamento de pré-processamento que injeta os dados do usuário autenticado na view — esse mecanismo é implementado por herança e está presente em todos os outros controllers do projeto

O item ativo da barra de navegação deve ser destacado corretamente para a seção de produtos.

### Internacionalização

Todos os textos visíveis ao usuário nas novas views devem ser externalizados nos arquivos de mensagens existentes. Tanto o arquivo em português quanto o arquivo em inglês devem ser atualizados com as novas chaves. A convenção de nomenclatura das chaves deve seguir o padrão já estabelecido nos arquivos existentes.


## Critérios de Avaliação e Distribuição de Pontos

### 1. Infraestrutura e Entidade (1,75 ponto)

| Critério | Pontos |
|---|--------|
| Entidade `Produto` mapeada corretamente com os campos descritos e o tipo de identificador adequado | 0,5    |
| Script de migration criado com numeração sequencial, tipo de dado correto para o identificador e sem dependência de criação automática de schema | 0,75   |
| `ProdutoRepository` estendendo a interface correta, sem métodos desnecessários | 0,25   |
| `ddl-auto` configurado conforme o padrão adotado pelo projeto a partir da aula de Flyway | 0,25   |

### 2. Camada de Serviço (1,25 ponto)

| Critério | Pontos |
|---|--------|
| Interface de serviço com os métodos do contrato descrito neste enunciado | 0,5    |
| Implementação respeitando o princípio de depender de abstrações e não de implementações concretas | 0,25   |
| Método de busca por código lançando a exceção correta quando o produto não existe | 0,25   |
| Operação de salvamento unificada para criação e edição | 0,25   |

### 3. DTO de Produto (1,5 ponto)

| Critério | Pontos |
|---|--------|
| Implementado como tipo imutável, seguindo o padrão dos demais DTOs do projeto | 0,75   |
| Métodos do contrato implementados corretamente: `from(entidade)`, `from(lista)`, `toEntity()`, `empty(código)` | 0,75   |

### 4. Controller (2,0 pontos)

| Critério | Pontos |
|---|---|
| Controller herda o mecanismo de pré-processamento de dados do usuário autenticado | 0,25 |
| Rota de listagem retornando todos os produtos via model e view correta | 0,25 |
| Rota de detalhe com comportamento de "buscar ou criar" conforme descrito no enunciado | 0,5 |
| Rota de salvamento com redirecionamento correto após persistência (padrão PRG) | 0,25 |
| Rota de exclusão funcionando corretamente | 0,25 |
| Captura de dados da requisição com as anotações adequadas para cada caso (path variable, parâmetro de formulário, objeto de formulário) | 0,5 |

### 5. Views Thymeleaf (2,0 pontos)

| Critério | Pontos |
|---|---|
| Todas as views com a declaração do namespace de templates | 0,25 |
| Navbar integrada via mecanismo de reutilização de templates com item ativo correto | 0,25 |
| Listagem com iteração sobre a coleção de produtos usando o recurso correto de templates | 0,5 |
| Exibição condicional para lista vazia e lista com itens | 0,25 |
| Formulário com vinculação bidirecional de dados do DTO ao formulário | 0,5 |
| Todos os textos visíveis externalizados nas chaves de internacionalização | 0,25 |

### 6. Segurança (1,5 ponto)

| Critério | Pontos |
|---|---|
| Role definida com nomenclatura consistente com a convenção do projeto | 0,25 |
| Configuração de segurança atualizada com a nova regra de rota na ordem correta | 0,5 |
| Rotas de escrita protegidas e rota de listagem acessível a autenticados sem a role específica | 0,5 |
| Redirecionamento para página de acesso negado funcionando para usuário sem a role | 0,25 |



## Restrições e Observações

- O projeto deve compilar e executar com `./mvnw spring-boot:run` sem erros
- O banco de dados deve subir com `docker compose up -d` e as migrations devem ser aplicadas automaticamente na inicialização
- Credenciais de banco de dados e do GitHub OAuth não devem ser commitadas — use variáveis de ambiente ou o `application.properties` local (que deve estar no `.gitignore`)
- Não é obrigatório implementar a integração entre Produto e Pedido

