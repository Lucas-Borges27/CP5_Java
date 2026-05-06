# produtos-ms

Microsservico Spring Boot para gestao de produtos, com interface Thymeleaf, persistencia em MySQL, migrations com Flyway e autenticacao por login local ou OAuth2 com GitHub.

## Visao geral

O projeto entrega:

- listagem de produtos para qualquer usuario autenticado
- cadastro, edicao e exclusao de produtos para usuarios com `ROLE_PRODUTO`
- cadastro de usuario local pela interface
- login local com usuario/senha
- login via GitHub
- internacionalizacao com troca de idioma por cookie (`pt-BR` e `en`)

## Stack

- Java 21
- Spring Boot 3.4.4
- Spring MVC
- Spring Security
- Spring Data JPA
- Spring OAuth2 Client
- Thymeleaf
- Flyway
- MySQL 8
- Docker Compose
- Maven Wrapper (`./mvnw`)

## Estrutura principal

```text
produtos-ms/
├── compose.yaml
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/br/com/fiap/produtosms/
    │   │   ├── configs/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   ├── entities/
    │   │   ├── repositories/
    │   │   ├── service/
    │   │   └── utils/
    │   └── resources/
    │       ├── application.properties
    │       ├── db/migration/
    │       ├── messages*.properties
    │       ├── static/
    │       └── templates/
    └── test/
```

## Pre-requisitos

- Java 21 ou superior
- Docker e Docker Compose
- Maven ou uso do wrapper `./mvnw`

## Banco de dados

O `compose.yaml` sobe um MySQL com a configuracao abaixo:

- host: `localhost`
- porta: `3307`
- database: `produtosdb`
- usuario: `root`
- senha: `root`

Comando:

```bash
docker compose up -d
```

As migrations do Flyway sao aplicadas na inicializacao da aplicacao:

1. `V1__create_usuario.sql`
2. `V2__create_produto.sql`
3. `V3__seed_admin_user.sql`
4. `V4__add_senha_to_usuario.sql`
5. `V5__seed_local_test_user.sql`

Usuario local de teste criado pela migration `V5`:

- login: `user@duckbiil.com`
- senha: `123`
- role: `ROLE_USER`

## Autenticacao e autorizacao

### Login local

Usuarios cadastrados pela tela `/cadastro` recebem `ROLE_USER`.

Permissoes desse perfil:

- acessar `/`
- acessar `/produtos`
- nao pode criar, editar ou excluir produtos

### Login com GitHub

O botao de GitHub usa OAuth2. No primeiro login, o usuario e persistido na tabela `usuario` e recebe `ROLE_PRODUTO`.

Permissoes desse perfil:

- acessar `/`
- acessar `/produtos`
- acessar `/produtos/novo`
- acessar `/produtos/detalhe/{codigo}`
- executar `POST /produtos/salvar`
- executar `POST /produtos/excluir`

## Configuracao do GitHub OAuth2

O login local funciona sem GitHub. O login via GitHub exige configurar estas variaveis:

```bash
export OAUTH_PRODUTOS_MS_CLIENT_ID_GIT=seu_client_id
export OAUTH_PRODUTOS_MS_SECRET_ID_GIT=seu_client_secret
```

Sugestao de app OAuth no GitHub para ambiente local:

- Homepage URL: `http://localhost:8080`
- Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`

Se essas variaveis nao forem definidas, o botao de login com GitHub nao vai autenticar corretamente.

## Como executar

1. Suba o banco:

```bash
docker compose up -d
```

2. Rode a aplicacao:

```bash
./mvnw spring-boot:run
```

3. Acesse:

```text
http://localhost:8080
```

## Como testar

Para rodar os testes, o MySQL precisa estar ativo em `localhost:3307`, porque existe teste de contexto que sobe a aplicacao com Flyway e datasource reais.

```bash
docker compose up -d
./mvnw test
```

## Rotas relevantes

Rotas publicas:

- `GET /login`
- `GET /cadastro`
- `POST /cadastro`
- `GET /oauth2/authorization/github`

Rotas autenticadas:

- `GET /`
- `GET /produtos`

Rotas com `ROLE_PRODUTO`:

- `GET /produtos/novo`
- `GET /produtos/detalhe/{codigo}`
- `POST /produtos/salvar`
- `POST /produtos/excluir`

## Internacionalizacao

A troca de idioma usa o parametro `lang` e salva a preferencia no cookie `produtos-ms-lang`.

Exemplos:

- `http://localhost:8080/?lang=pt-BR`
- `http://localhost:8080/?lang=en`

Arquivos de mensagens presentes no projeto:

- `messages.properties`
- `messages_pt.properties`
- `messages_pt_BR.properties`
- `messages_en.properties`

## Acesso ao MySQL

Para abrir o banco no container:

```bash
docker compose exec mysql mysql -uroot -proot produtosdb
```

## Observacoes

- o projeto usa `spring.jpa.hibernate.ddl-auto=none`; a estrutura depende das migrations
- o `README` antigo mencionava profile local, Java 17 e fluxo de roles manual, mas isso nao representa mais o comportamento atual do codigo
