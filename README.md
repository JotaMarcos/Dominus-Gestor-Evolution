# Dominus Gestor - Sistema Web de Gestao Financeira e MFA

O **Dominus Gestor** é uma aplicação web corporativa de alta performance desenvolvida para gestão financeira, controle de clientes e fornecedores, e geração de relatórios gerenciais em múltiplos formatos.

Projetado com uma arquitetura nativa em Java 21, o sistema dispensa frameworks pesados no backend e usa o servidor HTTP interno da linguagem.

---

## 🛠️ Tecnologias Utilizadas

### **Backend & Infraestrutura**

- **Java 21 Nativo**: Servidor Web HTTP (`com.sun.net.httpserver`) e Virtual Threads.
- **PostgreSQL 16**: Banco de dados relacional com modelagem para RBAC e MFA.
- **HikariCP**: Pool de conexões JDBC de altíssimo desempenho.
- **JasperReports Engine (v6.21.2)**: Motor de relatórios para exportação em **PDF, XLSX, DOCX, CSV e TXT**.
- **Google Authenticator (TOTP)**: Suporte nativo à Autenticação Multifator (2FA/MFA).
- **Docker & Docker Compose**: Containerização completa da aplicação e do banco de dados com build multi-estágio (_multi-stage build_).
- **Apache Maven**: Gerenciamento de dependências e empacotamento (`shade-plugin`).

### **Frontend**

- **HTML5 & CSS3 Moderno**: Interface limpa, responsiva e otimizada.
- **JavaScript (Vanilla)**: Manipulação nativa da DOM, integração via `fetch` API e gerenciamento de downloads de relatórios sem dependências externas.

---

## ✨ Principais Funcionalidades

- 🔒 **Autenticação & Segurança Robustas**:
  - Controle de Acesso Baseado em Funções (**RBAC**) com perfis de `ADMINISTRADOR`, `GERENTE` e `OPERADOR`.
  - Autenticação Multifator (**MFA / 2FA**) configurável via TOTP (Google Authenticator / Authy).
- 💼 **Gestão de Clientes & Fornecedores**:
  - Cadastro completo de dados empresariais (CNPJ, Inscrição Estadual, Contatos, Endereço).
- 💰 **Módulo Financeiro & Fluxo de Caixa**:
  - Controle de lançamentos (receitas/despesas), categorização com auto-relacionamento e gestão de contas bancárias.
- 📊 **Central de Relatórios Multiformato (JasperReports)**:
  - Exportação dinâmica de demonstrativos e listagens nos formatos **PDF, Excel (XLSX), Word (DOCX), CSV e TXT**.

---
## 📂 Estrutura do Projeto
## 📂 Estrutura do Projeto

```text
dominus-gestor/
├── 📄 docker-compose.yml             # Orquestração do PostgreSQL e Aplicação Java
├── 📄 Dockerfile                     # Build multi-stage (Maven + Temurin JRE)
├── 📄 pom.xml                        # Configurações do Maven e dependências
└── 📁 src/
    └── 📁 main/
        ├── 📁 java/br/com/dominus/
        │   ├── 📄 Main.java          # Entrypoint HTTP Server (Virtual Threads)
        │   ├── 📁 config/            # Pool HikariCP e Filtros de Segurança
        │   ├── 📁 controller/        # Handlers REST (Auth, Clientes, Financeiro, Relatórios)
        │   ├── 📁 dao/               # Data Access Objects (JDBC Puro)
        │   └── 📁 service/           # Serviços para MFA (TOTP) e JasperReports Engine
        └── 📁 resources/
            ├── 📁 db/
            │   └── 📄 schema.sql     # Script DDL com RBAC e tabelas MFA
            ├── 📁 reports/           # Templates de Relatórios (.jrxml) do Jaspersoft Studio
            └── 📁 webapp/            # Frontend SPA (HTML5, CSS3, JS Vanilla)
                ├── 📄 index.html     # Tela de Login com suporte a MFA
                ├── 📄 admin.html     # Painel Administrador
                ├── 📄 gerente.html   # Painel Gerencial e Exportador
                ├── 📄 sistema.html   # Módulo de Cadastro
                ├── 📁 css/
                │   └── 📄 style.css  # Estilização
                └── 📁 js/
                    ├── 📄 app.js      # Rotas e Interceptador
                    ├── 📄 auth.js     # Lógica MFA / TOTP
                    └── 📄 reports.js  # Gerenciador de Downloads
```
---

## 🚀 Como Executar o Projeto
### Pré-requisitos
- **Docker** e **Docker Compose** instalados.

### Passos para Execução
1. **Clonar o repositório:**
git clone https://github.com/JotaMarcos/dominus-gestor.git
cd dominus-gestor

2. **Subir os containers da Aplicação e do PostgreSQL:**
cp .env.example .env
mkdir -p secrets
printf '%s\n' 'defina-uma-senha-forte' > secrets/postgres_password.txt
docker compose up --build -d

3. **Acessar a Aplicação:**
- **Web UI:** [http://localhost:8080](http://localhost:8080)
- **PostgreSQL:** `localhost:5432` _(Database: `dominus_db` | User: `dominus`)_

---

## 👨‍💻 Autor
Desenvolvido por **[João Marcos Aires Duarte](https://github.com/JotaMarcos)**.

---
