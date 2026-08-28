-- ============================================================================
-- SCRIPT DE BANCO DE DADOS POSTGRESQL - DOMINUS GESTOR
-- ============================================================================

CREATE TABLE IF NOT EXISTS perfil (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO perfil (id, nome) VALUES
    (1, 'ADMINISTRADOR'),
    (2, 'GERENTE'),
    (3, 'OPERADOR')
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    login VARCHAR(100) UNIQUE,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    id_perfil INTEGER NOT NULL REFERENCES perfil(id),
    mfa_habilitado BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(64),
    mfa_tipo VARCHAR(20) DEFAULT 'TOTP',
    pin_seguranca VARCHAR(6),
    situacao VARCHAR(10) DEFAULT 'ATIVO' CHECK (situacao IN ('ATIVO', 'INATIVO'))
);

-- O administrador inicial é criado exclusivamente pelo banco de dados.
INSERT INTO usuario (nome, login, email, senha_hash, id_perfil, mfa_habilitado, situacao)
VALUES ('Administrador do Sistema', 'Admin', 'admin@dominus.com.br', '$2a$12$hemJldWDuCR5HLqtUhPB5OA0YbgrX8Z7ebN4FllZ6q0sQL5ir3d/2', 1, FALSE, 'ATIVO')
ON CONFLICT (email) DO NOTHING;

CREATE TABLE IF NOT EXISTS cliente (
    id SERIAL PRIMARY KEY,
    nome_empresarial VARCHAR(255) NOT NULL,
    cnpj VARCHAR(18) NOT NULL,
    ie VARCHAR(15),
    email VARCHAR(255),
    website VARCHAR(255),
    telefone VARCHAR(15),
    cep VARCHAR(9),
    endereco VARCHAR(255),
    numero VARCHAR(255),
    complemento VARCHAR(255),
    bairro VARCHAR(255),
    cidade VARCHAR(255),
    estado CHAR(2),
    nota INTEGER,
    situacao VARCHAR(10) NOT NULL DEFAULT 'ATIVO' CHECK (situacao IN ('ATIVO', 'INATIVO'))
);

CREATE TABLE IF NOT EXISTS contato (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    departamento VARCHAR(255),
    cpf VARCHAR(14),
    email VARCHAR(255),
    telefone VARCHAR(15),
    situacao VARCHAR(10) NOT NULL DEFAULT 'ATIVO' CHECK (situacao IN ('ATIVO', 'INATIVO')),
    id_cliente INTEGER NOT NULL REFERENCES cliente(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fornecedor (
    id SERIAL PRIMARY KEY,
    nome_empresarial VARCHAR(255) NOT NULL,
    cnpj VARCHAR(18) NOT NULL,
    ie VARCHAR(15),
    email VARCHAR(255),
    website VARCHAR(255),
    telefone VARCHAR(15),
    cep VARCHAR(9),
    endereco VARCHAR(255),
    numero VARCHAR(255),
    complemento VARCHAR(255),
    bairro VARCHAR(255),
    cidade VARCHAR(255),
    estado CHAR(2),
    nota INTEGER,
    situacao VARCHAR(10) NOT NULL DEFAULT 'ATIVO' CHECK (situacao IN ('ATIVO', 'INATIVO'))
);

CREATE TABLE IF NOT EXISTS conta (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    agencia VARCHAR(255) NOT NULL,
    conta_corrente VARCHAR(255) NOT NULL,
    data_inclusao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacao TEXT,
    situacao VARCHAR(10) NOT NULL DEFAULT 'ATIVO' CHECK (situacao IN ('ATIVO', 'INATIVO'))
);

CREATE TABLE IF NOT EXISTS categoria (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    situacao VARCHAR(10) NOT NULL DEFAULT 'ATIVO' CHECK (situacao IN ('ATIVO', 'INATIVO')),
    debito BOOLEAN DEFAULT FALSE,
    credito BOOLEAN DEFAULT FALSE,
    id_pai INTEGER REFERENCES categoria(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS lancamento (
    id SERIAL PRIMARY KEY,
    parcela INTEGER NOT NULL DEFAULT 1,
    total_parcelas INTEGER NOT NULL DEFAULT 1,
    valor NUMERIC(15,2) NOT NULL,
    data_lancamento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_vencimento TIMESTAMP NOT NULL,
    descricao TEXT,
    situacao VARCHAR(10) NOT NULL DEFAULT 'PENDENTE' CHECK (situacao IN ('PAGO', 'PENDENTE')),
    id_conta INTEGER NOT NULL REFERENCES conta(id),
    id_categoria INTEGER NOT NULL REFERENCES categoria(id),
    id_cliente INTEGER REFERENCES cliente(id),
    id_fornecedor INTEGER REFERENCES fornecedor(id)
);
