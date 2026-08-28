# Dominus Gestor - Sistema Web de Gestao Financeira e MFA

O **Dominus Gestor** é uma aplicação web corporativa de alta performance desenvolvida para gestão financeira, controle de clientes e fornecedores, e geração de relatórios gerenciais em múltiplos formatos.

Projetado com Quarkus 3 e Java 21, o sistema usa RESTEasy Reactive, CDI e o pool Agroal para entregar uma aplicação leve e eficiente.

---

## 🛠️ Tecnologias Utilizadas

### **Backend & Infraestrutura**

- **Quarkus 3**: API REST reativa, CDI e empacotamento fast-jar.
- **PostgreSQL 16**: Banco de dados relacional com modelagem para RBAC e MFA.
- **Agroal**: Pool de conexões JDBC gerenciado pelo Quarkus.
- **JasperReports Engine (v7.0.7)**: Motor de relatórios para exportação em **PDF, XLSX, DOCX, CSV e TXT**.
- **Google Authenticator (TOTP)**: Suporte nativo à Autenticação Multifator (2FA/MFA).
- **Docker & Docker Compose**: Containerização completa da aplicação e do banco de dados com build multi-estágio (_multi-stage build_).
- **Apache Maven**: Gerenciamento de dependências e empacotamento (`shade-plugin`).

### **Frontend**

- **HTML5 & CSS3 Moderno**: Interface limpa, responsiva e otimizada.
- **JavaScript (Vanilla)**: Manipulação nativa do DOM, integração via `fetch` API e gerenciamento de downloads sem dependências externas.

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

```text
dominus-gestor-evolution/
├── 📄 docker-compose.yml             # Orquestração do PostgreSQL e Aplicação Java
├── 📄 Dockerfile                     # Build multi-stage (Maven + Temurin JRE)
├── 📁 backend/
│   ├── 📄 pom.xml                    # Configurações do Maven e dependências
│   └── 📁 src/
│       └── 📁 main/
│           ├── 📁 java/br/com/dominus/
        │   ├── 📁 config/            # Configuração de persistência e aplicação
        │   ├── 📁 controller/        # Recursos REST (Auth, Clientes, Financeiro, Relatórios)
      │   └── 📁 service/           # Serviços para MFA (TOTP) e JasperReports Engine
      └── 📁 resources/
        ├── 📁 db/             # Script DDL com RBAC e tabelas MFA
        └── 📁 reports/        # Templates de Relatórios (.jrxml)
└── 📁 frontend/                       # Frontend SPA (HTML5, CSS3, JS Vanilla)
  ├── 📄 index.html                  # Tela de Login com suporte a MFA
  ├── 📄 admin.html                  # Painel Administrador
  ├── 📄 gerente.html                # Painel Gerencial e Exportador
  ├── 📄 sistema.html                # Módulo de Cadastro
  ├── 📁 css/                        # Estilização
  └── 📁 js/                         # Lógica MFA e relatórios
```

---

## 🚀 Como Executar o Projeto

### Pré-requisitos

- **Docker** e **Docker Compose** instalados.

### Passos para Execução

1. **Clonar o repositório:**
   git clone https://github.com/JotaMarcos/Dominus-Gestor-Evolution.git
   cd dominus-gestor-evolution

2. **Subir os containers da Aplicação e do PostgreSQL:**
   cp .env.example .env
   mkdir -p secrets
   printf '%s\n' 'defina-uma-senha-forte' > secrets/postgres_password.txt
   docker compose up --build -d

3. **Acessar a Aplicação:**

- **Web UI:** [http://localhost:8080](http://localhost:8080)
- **PostgreSQL:** `localhost:5432` _(Database: `dominus_db` | User: `dominus`)_

O backend Quarkus serve o frontend diretamente em `META-INF/resources`, mantendo a aplicação organizada exclusivamente nos diretórios `backend/` e `frontend/`.

### Execução local sem Docker

No Windows, execute `start-dev.bat` ou `start-dev.ps1`. O script detecta automaticamente o Docker: com Docker, sobe o Compose completo; sem Docker, inicia o Quarkus no perfil `dev` com um banco H2 temporário compatível com PostgreSQL.

---

## 👨‍💻 Autor

Desenvolvido por **[João Marcos Aires Duarte](https://github.com/JotaMarcos)**.

---
