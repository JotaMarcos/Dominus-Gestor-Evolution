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
- Sessão autenticada por JWT assinado (RS256), entregue em cookie `HttpOnly`, `SameSite=Strict` (e `Secure` fora do perfil dev) — nunca exposto ao JavaScript do navegador.
- Todos os endpoints de negócio exigem sessão válida; endpoints não anotados são negados por padrão (`quarkus.security.jaxrs.deny-unannotated-endpoints`).
- Perfis `ADMINISTRADOR`, `GERENTE` e `OPERADOR`.
- Cadastro público restrito a usuários `OPERADOR`.
- Administrador inicial criado exclusivamente via banco de dados.
- Clientes, fornecedores e lançamentos financeiros — **endpoints REST hoje retornam dados de exemplo fixos (mock)**; a persistência real ainda será implementada.
- Relatórios em PDF, XLSX, DOCX, CSV e TXT.

> **Status de segurança conhecido:** `/api/auth/mfa/toggle` ainda não está implementado (sempre responde `501`); a emissão de sessão após MFA já funciona (`/api/auth/mfa/verify`).

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
│       │   ├── security/        # Emissão de JWT e cookie de sessão
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

### Com um clique (recomendado)

Na raiz do projeto, no Windows, dê duplo clique em `start-dev.bat` (ou execute `.\start-dev.ps1` no PowerShell). O script:

1. Detecta se o Docker está disponível na máquina.
2. **Com Docker**: na primeira vez, gera sozinho `secrets/postgres_password.txt` e o par de chaves JWT (`secrets/jwt-private-key.pem`/`jwt-public-key.pem`) — nunca versionados — e sobe `docker compose up --build` (PostgreSQL real + aplicação).
3. **Sem Docker**: localiza o Maven (PATH, wrapper ou instalação conhecida) e inicia `mvn quarkus:dev` direto em `backend/`, usando H2 temporário compatível com PostgreSQL.

Em ambos os casos a aplicação sobe sozinha em [http://localhost:8080](http://localhost:8080), sem passos manuais.

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

Em produção, configure `DB_URL`, `DB_USER` e `DB_PASS`, ou `DB_PASS_FILE` no Docker. O PostgreSQL é o banco oficial e o Quarkus gerencia o pool Agroal. O H2 é usado nos perfis `dev` e `test` (efêmero, recriado a cada execução/teste).

### Sessão e autenticação (JWT)

Fora dos perfis `dev`/`test` (que usam um par de chaves de conveniência versionado apenas para esses perfis), é **obrigatório** configurar:

- `JWT_PRIVATE_KEY_LOCATION`: localização (`file:...`) da chave privada RSA usada para assinar a sessão.
- `JWT_PUBLIC_KEY_LOCATION`: localização (`file:...`) da chave pública correspondente, usada para validar a sessão.

Sem essas variáveis, a aplicação falha ao iniciar fora do perfil dev/test — por design, não existe um par de chaves padrão em produção. O cookie de sessão (`dominus_session`) é `HttpOnly` e `SameSite=Strict`, e recebe o atributo `Secure` automaticamente no perfil `prod`.

### Identidade visual

A aplicação e seus textos estão em português do Brasil. O frontend usa somente estilos e elementos próprios do Dominus Gestor, sem logos, imagens, ícones ou fontes de frameworks externos.

---

## Autor

Desenvolvido por **[João Marcos Aires Duarte](https://github.com/JotaMarcos)**.
