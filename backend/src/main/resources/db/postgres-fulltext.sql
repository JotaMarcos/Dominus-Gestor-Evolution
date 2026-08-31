-- ============================================================================
-- ÍNDICE DE BUSCA FULL-TEXT (exclusivo do PostgreSQL)
-- ============================================================================
-- Aplicado somente ao PostgreSQL real, como script de inicialização do
-- container (ver docker-compose.yml). O perfil dev/test usa H2, que não tem
-- tsvector/GIN — nesse caso a busca por nome do cliente cai para ILIKE
-- (ver ClienteController, que decide o modo pela config quarkus.datasource.db-kind).
CREATE INDEX IF NOT EXISTS idx_cliente_busca_fulltext
    ON cliente
    USING GIN (to_tsvector('portuguese', nome_empresarial));
