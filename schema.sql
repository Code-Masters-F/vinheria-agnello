-- ============================================
-- VINHEIRO - Schema PostgreSQL
-- ============================================

-- Tenant central
CREATE TABLE vinheria (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(100) NOT NULL,
    slug            VARCHAR(60) UNIQUE NOT NULL,
    logo_url        VARCHAR(255),
    cor_primaria    VARCHAR(7),
    cor_secundaria  VARCHAR(7),
    ativo           BOOLEAN DEFAULT TRUE,
    criado_em       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Usuário da vinheria (acesso ao painel B2B)
CREATE TABLE usuario_admin (
    id              BIGSERIAL PRIMARY KEY,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    nome            VARCHAR(100) NOT NULL,
    email           VARCHAR(150) UNIQUE NOT NULL,
    senha_hash      VARCHAR(255) NOT NULL,
    criado_em       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Catálogo de vinhos por vinheria
CREATE TABLE vinho (
    id              BIGSERIAL PRIMARY KEY,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    nome            VARCHAR(150) NOT NULL,
    tipo            VARCHAR(20) NOT NULL,
    uva             VARCHAR(100),
    pais            VARCHAR(80),
    regiao          VARCHAR(100),
    safra           INTEGER,
    preco           DECIMAL(10,2) NOT NULL,
    descricao       TEXT,
    foto_url        VARCHAR(255),
    estoque         INT DEFAULT 0,
    estoque_minimo  INT DEFAULT 3,
    ativo           BOOLEAN DEFAULT TRUE,
    criado_em       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ocasiões e harmonizações (tags M:N)
CREATE TABLE vinho_ocasiao (
    vinho_id   BIGINT REFERENCES vinho(id),
    ocasiao    VARCHAR(30),
    PRIMARY KEY (vinho_id, ocasiao)
);

-- Cliente consumidor (B2C)
CREATE TABLE cliente (
    id              BIGSERIAL PRIMARY KEY,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    nome            VARCHAR(100) NOT NULL,
    email           VARCHAR(150),
    whatsapp        VARCHAR(20),
    cpf             VARCHAR(14),
    endereco        TEXT,
    senha_hash      VARCHAR(255),
    tipo_cadastro   VARCHAR(20) NOT NULL,
    pontos          INT DEFAULT 0,
    criado_em       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (vinheria_id, email),
    UNIQUE (vinheria_id, whatsapp)
);

-- Pedidos
CREATE TABLE pedido (
    id              BIGSERIAL PRIMARY KEY,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    cliente_id      BIGINT REFERENCES cliente(id),
    status          VARCHAR(30) DEFAULT 'aguardando_pagamento',
    tipo_entrega    VARCHAR(20) NOT NULL,
    subtotal        DECIMAL(10,2) NOT NULL,
    total           DECIMAL(10,2) NOT NULL,
    endereco_entrega TEXT,
    criado_em       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE item_pedido (
    id          BIGSERIAL PRIMARY KEY,
    pedido_id   BIGINT NOT NULL REFERENCES pedido(id),
    vinho_id    BIGINT NOT NULL REFERENCES vinho(id),
    quantidade  INT NOT NULL,
    preco_unit  DECIMAL(10,2) NOT NULL
);

-- Pagamentos
CREATE TABLE pagamento (
    id              BIGSERIAL PRIMARY KEY,
    pedido_id       BIGINT NOT NULL REFERENCES pedido(id),
    metodo          VARCHAR(20),
    status          VARCHAR(20),
    valor           DECIMAL(10,2) NOT NULL,
    gateway_id      VARCHAR(100),
    criado_em       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Avaliações de vinho pelo cliente
CREATE TABLE avaliacao_vinho (
    id          BIGSERIAL PRIMARY KEY,
    cliente_id  BIGINT NOT NULL REFERENCES cliente(id),
    vinho_id    BIGINT NOT NULL REFERENCES vinho(id),
    nota        SMALLINT CHECK (nota BETWEEN 1 AND 5),
    ocasiao     VARCHAR(60),
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (cliente_id, vinho_id)
);

-- Campanhas segmentadas
CREATE TABLE campanha (
    id              BIGSERIAL PRIMARY KEY,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    nome            VARCHAR(150) NOT NULL,
    mensagem        TEXT NOT NULL,
    filtro_tipo     VARCHAR(60),
    canal           VARCHAR(20),
    status          VARCHAR(20),
    enviada_em      TIMESTAMP,
    criado_em       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Programa de fidelidade — configuração por vinheria
CREATE TABLE config_fidelidade (
    vinheria_id         BIGINT PRIMARY KEY REFERENCES vinheria(id),
    pontos_por_real     DECIMAL(5,2) DEFAULT 1.0,
    validade_dias       INT DEFAULT 365,
    recompensas         JSONB
);

-- Histórico de pontos
CREATE TABLE historico_pontos (
    id          BIGSERIAL PRIMARY KEY,
    cliente_id  BIGINT NOT NULL REFERENCES cliente(id),
    pedido_id   BIGINT REFERENCES pedido(id),
    pontos      INT NOT NULL,
    descricao   VARCHAR(200),
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Análise de QR Code (dados anônimos)
CREATE TABLE scan_qrcode (
    id              BIGSERIAL PRIMARY KEY,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    ocasiao         VARCHAR(60),
    faixa_preco     VARCHAR(30),
    converteu       BOOLEAN DEFAULT FALSE,
    criado_em       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_vinho_vinheria ON vinho(vinheria_id);
CREATE INDEX idx_cliente_vinheria ON cliente(vinheria_id);
CREATE INDEX idx_pedido_vinheria ON pedido(vinheria_id);
CREATE INDEX idx_pedido_cliente ON pedido(cliente_id);
CREATE INDEX idx_item_pedido_pedido ON item_pedido(pedido_id);
