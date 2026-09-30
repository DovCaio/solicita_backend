# Solicita

Sistema web para gerenciamento de solicitações internas.

## Tecnologias

### Backend

- Java 21
- Spring Boot
- Spring Security
- PostgreSQL
- Flyway

### Frontend

- React
- TypeScript

## Funcionalidades

- Autenticação
- Cadastro de solicitações
- Edição e exclusão
- Gerenciamento de status
- Filtros
- Dashboard

## Como executar

...

## Arquitetura

...

## Decisões técnicas

...

## Testes

### Os testes unitários do service de request valida:

- regras de criação e associação do usuário;
- consulta e tratamento de recursos inexistentes;
- regras de edição e exclusão;
- alteração de status;
- paginação e limite de requisições;
- mapeamento das entidades para DTOs.

## Screenshots

...

## Tabela de dados

| Tabela   | Campo       | Tipo         | Obrigatório | Descrição                    |
| -------- | ----------- | ------------ | ----------- | ---------------------------- |
| users    | id          | BIGSERIAL    | Sim         | Identificador do usuário     |
| users    | username    | VARCHAR(100) | Sim         | Nome de usuário              |
| users    | password    | VARCHAR(255) | Sim         | Senha armazenada com BCrypt  |
| requests | id          | BIGSERIAL    | Sim         | Identificador da solicitação |
| requests | title       | VARCHAR(150) | Sim         | Título da solicitação        |
| requests | description | TEXT         | Sim         | Descrição                    |
| requests | category    | VARCHAR(30)  | Sim         | Categoria                    |
| requests | status      | VARCHAR(30)  | Sim         | Status atual                 |
| requests | created_at  | TIMESTAMP    | Sim         | Data de criação              |
| requests | updated_at  | TIMESTAMP    | Não         | Data da última atualização   |
| requests | user_id     | BIGINT       | Sim         | Usuário solicitante          |
