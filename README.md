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
- **BCrypt (jbcrypt)**: hash de senhas, nunca armazenadas em texto puro.
- **Docker e Docker Compose**: PostgreSQL e aplicação em containers.
- **Apache Maven**: dependências, testes e empacotamento Quarkus fast-jar.

### **Frontend**

- **HTML5 & CSS3 Moderno**: Interface limpa, responsiva e otimizada.
- **JavaScript (Vanilla)**: integração via API `fetch`, sem dependências externas.

---

## Funcionalidades

### Autenticação e segurança

- Login por identificador (login) ou e-mail.
- Senhas armazenadas somente como hash BCrypt.
- Sessão autenticada por JWT assinado (RS256), entregue em cookie `HttpOnly`, `SameSite=Strict` (e `Secure` fora do perfil dev) — nunca exposto ao JavaScript do navegador.
- Todos os endpoints de negócio exigem sessão válida; endpoints não anotados são negados por padrão (`quarkus.security.jaxrs.deny-unannotated-endpoints`).
- Autenticação multifator (TOTP / Google Authenticator) verificada em `/api/auth/mfa/verify` antes da emissão da sessão, quando habilitada para o usuário.
- **Recuperação de senha sem precisar da senha atual** (`recuperar-senha.html`): o usuário informa login ou e-mail cadastrado; se o MFA estiver habilitado, precisa confirmar também o código do Authenticator; em seguida define a nova senha (mínimo 8 caracteres). Não depende de envio de e-mail — toda a verificação é feita com dados já cadastrados no sistema.
- Perfis `ADMINISTRADOR`, `GERENTE` e `OPERADOR`.
- Cadastro público (`cadastro.html`) restrito a usuários `OPERADOR`.
- Administrador inicial criado exclusivamente via banco de dados (nunca pela interface).

> **Status de segurança conhecido:** `/api/auth/mfa/toggle` ainda não está implementado (sempre responde `501`); a emissão de sessão após MFA já funciona (`/api/auth/mfa/verify`).

### Navegação

- Menu lateral montado por perfil em todas as telas internas (`js/nav.js`), com atalho **Início** para a página inicial do perfil e botão **Sair**.
- A barra lateral é fixa na tela (`position: sticky`), então o botão **Sair** permanece sempre visível mesmo em páginas com listas longas (ex.: 5.000 clientes paginados).
- O `ADMINISTRADOR` alterna livremente entre Usuários & MFA, Dashboard Gerencial e Clientes & Fornecedores a partir de qualquer página.

### Usuários

- `GET /api/usuarios` lista os usuários cadastrados (nome, login, e-mail, perfil, situação e MFA), renderizado dinamicamente em `admin.html`.
- `POST /api/usuarios` cadastra um novo usuário `OPERADOR` (uso público, via `cadastro.html`).

### Clientes & Fornecedores

- `GET /api/clientes` lista clientes com:
  - Paginação: 10 / 20 / 50 / 100 itens por página, com botões primeira, anterior, próxima e última página.
  - Ordenação crescente/decrescente por razão social ou CNPJ.
  - Busca por nome do cliente usando **Full-Text Search nativo do PostgreSQL** (`to_tsvector`/`plainto_tsquery` com índice GIN) quando o banco é PostgreSQL real. No perfil dev/test (H2, sem `tsvector`) a mesma API cai automaticamente para `ILIKE`, mantendo o comportamento idêntico para quem está testando sem Docker.
- Tela `sistema.html` (módulo "Clientes & Fornecedores") consome esse endpoint via `clientes.js`.

### Relatórios

- Relatórios em PDF, XLSX, DOCX, CSV e TXT, com o logotipo do Dominus Gestor Evolution no cabeçalho.
- Personalização opcional por cliente (`?cliente=nome`), disponível em `gerente.html` via campo "Personalizar por cliente" — filtra os dados do relatório para um único cliente.

---

## Estrutura do Projeto

```text
dominus-gestor-evolution/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/br/com/dominus/
│       │   ├── config/          # Datasource e inicialização dev/test
│       │   ├── controller/      # Recursos REST
│       │   │   ├── AuthController.java       # login, MFA, logout, recuperação de senha
│       │   │   ├── UsuarioController.java    # listagem e cadastro de usuários
│       │   │   ├── ClienteController.java    # listagem paginada/ordenada/buscável de clientes
│       │   │   ├── LancamentoController.java
│       │   │   └── RelatorioController.java  # exportação de relatórios Jasper
│       │   ├── security/        # Emissão de JWT e cookie de sessão
│       │   └── service/         # MFA e relatórios
│       └── resources/
│           ├── application.properties
│           ├── db/               # schema.sql (portável), dev-seed.sql (massa de
│           │                     # dados dev/test) e postgres-fulltext.sql
│           │                     # (índice GIN exclusivo do PostgreSQL real)
│           └── reports/          # Modelos JasperReports + logo-dominus.png
├── frontend/webapp/
│   ├── index.html                # Login
│   ├── cadastro.html             # Cadastro de operadores
│   ├── recuperar-senha.html      # Recuperação de senha sem senha antiga
│   ├── admin.html                # Usuários e MFA
│   ├── gerente.html              # Gestão e relatórios
│   ├── sistema.html              # Clientes e fornecedores
│   ├── css/style.css             # Estilos próprios
│   └── js/
│       ├── nav.js                # Menu lateral fixo por perfil (Início/Sair)
│       ├── auth.js               # Login e verificação de MFA
│       ├── cadastro.js           # Cadastro público de operadores
│       ├── recuperar-senha.js    # Fluxo de recuperação de senha
│       ├── clientes.js           # Listagem paginada/ordenável de clientes
│       ├── admin.js              # Lista de usuários
│       └── reports.js            # Exportação de relatórios (com filtro por cliente)
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

### Passo a passo (com um clique — recomendado)

1. Clone ou abra a pasta do projeto.
2. Na raiz do projeto, no Windows, dê duplo clique em `start-dev.bat` (ou execute `.\start-dev.ps1` no PowerShell).
3. O script decide sozinho o modo de execução:
   - **Com Docker instalado**: na primeira vez, gera automaticamente `secrets/postgres_password.txt` e o par de chaves JWT (`secrets/jwt-private-key.pem`/`jwt-public-key.pem`) — nunca versionados — e sobe `docker compose up --build` (PostgreSQL real + aplicação, já com a massa de dados de desenvolvimento carregada).
   - **Sem Docker**: localiza o Maven (PATH, wrapper ou instalação conhecida) e inicia `mvn quarkus:dev` direto em `backend/`, usando H2 em memória compatível com PostgreSQL (também populado com a mesma massa de dados).
4. Aguarde a aplicação subir e acesse **http://localhost:8080**.
5. Faça login com as [credenciais locais](#credenciais-locais) abaixo.

Nenhum passo manual é necessário em nenhum dos dois modos.

### Com Docker (manual)

Caso prefira não usar o script, na raiz de `dominus-gestor-evolution`:

1. Crie `secrets/postgres_password.txt` com uma senha forte.
2. Gere o par de chaves RSA usado para assinar a sessão (JWT) — nunca comitado, exclusivo deste ambiente:

   ```bash
   openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/jwt-private-key.pem
   openssl rsa -pubout -in secrets/jwt-private-key.pem -out secrets/jwt-public-key.pem
   ```

3. Execute:

   ```bash
   docker compose up --build
   ```

PostgreSQL: `localhost:5432`, banco `dominus_db`, usuário `dominus`.

O backend Quarkus serve o frontend diretamente em `META-INF/resources`, mantendo a aplicação organizada exclusivamente nos diretórios `backend/` e `frontend/`.

### Sem Docker (manual)

```powershell
cd backend
mvn quarkus:dev -Dquarkus.profile=dev -Dquarkus.analytics.disabled=true
```

Não execute Maven na raiz: o `pom.xml` fica em `backend/`.

### Rodando os testes automatizados

```powershell
cd backend
mvn test
```

Cobre login/MFA/recuperação de senha (`AuthControllerTest`), emissão de token (`TokenServiceTest`), seleção de datasource (`DatabaseConfigTest`) e geração de relatórios (`ReportServiceTest`).

### Credenciais locais

O administrador é criado pelo banco de dados, nunca pela tela:

- Login: `Admin`
- E-mail: `admin@dominus.com.br`
- Senha: `Toor#@!1439$10`

A senha é gravada somente como BCrypt. A tela `cadastro.html` permite criar usuários `OPERADOR`; não é possível criar administrador pela interface. Esqueceu a senha de algum usuário? Use `recuperar-senha.html` — veja [Recuperação de senha](#autenticação-e-segurança) acima.

### Banco de dados

Em produção, configure `DB_URL`, `DB_USER` e `DB_PASS`, ou `DB_PASS_FILE` no Docker. O PostgreSQL é o banco oficial e o Quarkus gerencia o pool Agroal. O H2 é usado nos perfis `dev` e `test` (efêmero, recriado a cada execução/teste).

### Massa de dados de desenvolvimento

`db/dev-seed.sql` é carregado automaticamente nos perfis `dev`/`test` (e também no primeiro `docker compose up`, via `docker-entrypoint-initdb.d`) e contém:

- **20 usuários** no total (1 `ADMINISTRADOR` criado pelo `schema.sql` com a senha oficial + 19 gerados: 3 `ADMINISTRADOR`, 6 `GERENTE`, 10 `OPERADOR`). Todos os 19 usam a senha **`Dominus@123`** — login pelo campo `login` mostrado em `admin.html` (ex.: `caio.almeida.5`).
- **5.000 clientes** com endereço, CNPJ, contato e situação variados, usados para validar busca, ordenação e paginação.
- **10.000 lançamentos financeiros** (2 por cliente, entre receitas e despesas), além das contas e categorias necessárias para os relatórios.

O índice `idx_cliente_busca_fulltext` (GIN sobre `to_tsvector('portuguese', nome_empresarial)`), usado pela busca full-text, só é aplicado ao PostgreSQL real (`db/postgres-fulltext.sql`) — o H2 não suporta `tsvector`.

### Sessão e autenticação (JWT)

Fora dos perfis `dev`/`test` (que usam um par de chaves de conveniência versionado apenas para esses perfis), é **obrigatório** configurar:

- `JWT_PRIVATE_KEY_LOCATION`: localização (`file:...`) da chave privada RSA usada para assinar a sessão.
- `JWT_PUBLIC_KEY_LOCATION`: localização (`file:...`) da chave pública correspondente, usada para validar a sessão.

Sem essas variáveis, a aplicação falha ao iniciar fora do perfil dev/test — por design, não existe um par de chaves padrão em produção. O cookie de sessão (`dominus_session`) é `HttpOnly` e `SameSite=Strict`, e recebe o atributo `Secure` automaticamente no perfil `prod`.

### Identidade visual

A aplicação e seus textos estão em português do Brasil. O frontend usa somente estilos e elementos próprios do Dominus Gestor, sem logos, imagens, ícones ou fontes de frameworks externos. O logotipo do Dominus Gestor Evolution (`reports/logo-dominus.png`) é usado exclusivamente no cabeçalho dos relatórios exportados (PDF/XLSX/DOCX/CSV/TXT).

---

## Endpoints principais da API

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/auth/login` | Autentica por login/e-mail + senha; retorna `mfaRequired` se o usuário tiver MFA habilitado. |
| `POST` | `/api/auth/mfa/verify` | Valida o código TOTP e emite a sessão (cookie `dominus_session`). |
| `POST` | `/api/auth/password/forgot` | Confirma que login/e-mail existe e está ativo; retorna se o MFA é obrigatório para a próxima etapa. |
| `POST` | `/api/auth/password/reset` | Define uma nova senha (com código MFA se aplicável), sem exigir a senha atual. |
| `POST` | `/api/auth/logout` | Encerra a sessão (limpa o cookie). |
| `GET` | `/api/usuarios` | Lista usuários cadastrados (requer sessão). |
| `POST` | `/api/usuarios` | Cadastra um novo usuário `OPERADOR` (público). |
| `GET` | `/api/clientes` | Lista clientes paginada, ordenável e buscável (requer sessão). Parâmetros: `q`, `page`, `pageSize` (10/20/50/100), `sort` (`nome_empresarial`/`cnpj`), `dir` (`asc`/`desc`). |
| `GET` | `/api/relatorios/exportar` | Exporta relatório Jasper. Parâmetros: `nome`, `formato` (`pdf`/`xlsx`/`docx`/`csv`/`txt`), `cliente` (opcional, filtra por nome). |

---

## Autor

Desenvolvido por **[João Marcos Aires Duarte](https://github.com/JotaMarcos)**.
