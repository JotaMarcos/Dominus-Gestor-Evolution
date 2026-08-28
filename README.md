# Dominus Gestor Evolution

Aplicação web em português do Brasil para gestão financeira, clientes, fornecedores, usuários, MFA e relatórios gerenciais.

O backend usa Quarkus 3.27, Java 21, REST, CDI e pool Agroal. O frontend usa HTML, CSS e JavaScript próprios, sem ícones, imagens ou dependências visuais de frameworks externos.

---

## Tecnologias

### **Backend & Infraestrutura**

- **Quarkus 3.27**: API REST, CDI e empacotamento fast-jar.
- **PostgreSQL 16**: Banco de dados relacional com modelagem para RBAC e MFA.
- **Agroal**: Pool de conexões JDBC gerenciado pelo Quarkus.
- **JasperReports Engine (v7.0.7)**: Motor de relatórios para exportação em **PDF, XLSX, DOCX, CSV e TXT**.
- **Google Authenticator (TOTP)**: autenticação multifator.
- **Docker e Docker Compose**: PostgreSQL e aplicação em containers.
- **Apache Maven**: dependências, testes e empacotamento Quarkus fast-jar.

### **Frontend**

- **HTML5 & CSS3 Moderno**: Interface limpa, responsiva e otimizada.
- **JavaScript (Vanilla)**: integração via API `fetch`, sem dependências externas.

---

## Funcionalidades

- Login por identificador ou e-mail.
- Senhas armazenadas somente como hash BCrypt.
- Perfis `ADMINISTRADOR`, `GERENTE` e `OPERADOR`.
- Cadastro público restrito a usuários `OPERADOR`.
- Administrador inicial criado exclusivamente via banco de dados.
- Clientes, fornecedores e lançamentos financeiros.
- Relatórios em PDF, XLSX, DOCX, CSV e TXT.

---

## Estrutura do Projeto

```text
dominus-gestor-evolution/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/br/com/dominus/
│       │   ├── config/          # Datasource e inicialização dev
│       │   ├── controller/      # Recursos REST
│       │   └── service/         # MFA e relatórios
│       └── resources/
│           ├── application.properties
│           ├── db/               # Schema e seed do administrador
│           └── reports/          # Modelos JasperReports
├── frontend/webapp/
│   ├── index.html                # Login
│   ├── cadastro.html             # Cadastro de operadores
│   ├── admin.html                # Usuários e MFA
│   ├── gerente.html              # Gestão e relatórios
│   ├── sistema.html              # Clientes e financeiro
│   ├── css/style.css             # Estilos próprios
│   └── js/                       # Comportamentos da interface
├── docker-compose.yml
├── Dockerfile
├── docker-entrypoint.sh
├── start-dev.bat
└── start-dev.ps1
```

---

## Como Executar Localmente

### Pré-requisitos

- Java 21 ou superior.
- Maven 3.9 ou Maven Wrapper.
- Docker é opcional.

### Com Docker

Na raiz de `dominus-gestor-evolution`, crie `secrets/postgres_password.txt` com uma senha forte e execute:

```bash
docker compose up --build
```

Aplicação: [http://localhost:8080](http://localhost:8080)

PostgreSQL: `localhost:5432`, banco `dominus_db`, usuário `dominus`.

O backend Quarkus serve o frontend diretamente em `META-INF/resources`, mantendo a aplicação organizada exclusivamente nos diretórios `backend/` e `frontend/`.

### Sem Docker

Na raiz do projeto, no Windows, execute `start-dev.bat` ou:

```powershell
.\start-dev.ps1
```

O script detecta o Docker automaticamente. Sem Docker, entra em `backend/`, inicia o Quarkus no perfil `dev` e usa H2 temporário compatível com PostgreSQL. O frontend é servido em [http://localhost:8080](http://localhost:8080).

Não execute Maven na raiz: o `pom.xml` fica em `backend/`.

### Execução manual

```powershell
cd backend
mvn quarkus:dev -Dquarkus.profile=dev -Dquarkus.analytics.disabled=true
```

### Credenciais locais

O administrador é criado pelo banco de dados, nunca pela tela:

- Login: `Admin`
- E-mail: `admin@dominus.com.br`
- Senha: `Toor#@!1439$10`

A senha é gravada somente como BCrypt. A tela `cadastro.html` permite criar usuários `OPERADOR`; não é possível criar administrador pela interface.

### Banco de dados

Em produção, configure `DB_URL`, `DB_USER` e `DB_PASS`, ou `DB_PASS_FILE` no Docker. O PostgreSQL é o banco oficial e o Quarkus gerencia o pool Agroal. O H2 é usado somente no perfil local `dev`.

### Identidade visual

A aplicação e seus textos estão em português do Brasil. O frontend usa somente estilos e elementos próprios do Dominus Gestor, sem logos, imagens, ícones ou fontes de frameworks externos.

---

## Autor

Desenvolvido por **[João Marcos Aires Duarte](https://github.com/JotaMarcos)**.
