# Dominus Gestor

Aplicacao web Java com servidor HTTP nativo, PostgreSQL, HikariCP e JasperReports.

## Requisitos

- Docker Desktop com Compose
- Java 21 apenas para desenvolvimento local

## Execucao com Docker

1. Copie `.env.example` para `.env`.
2. Crie `secrets/postgres_password.txt` e coloque somente a senha do PostgreSQL nesse arquivo. Esse arquivo não é versionado.
3. Execute `docker compose up --build`.
4. Acesse `http://localhost:8080`.

O primeiro usuario deve ser provisionado por uma rotina administrativa segura. O schema nao cria credenciais padrao.

## Variaveis obrigatorias da aplicacao

- `DB_URL`
- `DB_USER`
- `DB_PASS_FILE` (preferencial) ou `DB_PASS` apenas fora do Docker

Nunca versione `.env`, `secrets/postgres_password.txt`, senhas, tokens ou artefatos de `target/`.

## Validacao

```text
mvn clean verify
```

O build Docker usa Java 21 e executa os testes antes do empacotamento.
