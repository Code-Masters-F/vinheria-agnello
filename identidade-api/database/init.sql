-- Banco próprio do serviço de Identidade (Database per Service).
-- Executar conectado ao banco identidade_db.
-- Os hashes BCrypt são gerados pelo próprio PostgreSQL (pgcrypto, custo 10), compatíveis com BCrypt.Net.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS vinheria (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    slug        VARCHAR(60)  NOT NULL UNIQUE,
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS usuario (
    id          BIGSERIAL PRIMARY KEY,
    vinheria_id BIGINT       NOT NULL REFERENCES vinheria (id),
    nome        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    senha_hash  VARCHAR(255) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL CHECK (perfil IN ('Admin', 'Cliente')),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- E-mail único sem diferenciar maiúsculas/minúsculas (mesma regra da consulta de login).
CREATE UNIQUE INDEX IF NOT EXISTS ux_usuario_email ON usuario (lower(email));

-- Dados de demonstração
INSERT INTO vinheria (id, nome, slug)
VALUES (1, 'Vinheria Agnello', 'agnello')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('vinheria', 'id'), (SELECT max(id) FROM vinheria));

INSERT INTO usuario (vinheria_id, nome, email, senha_hash, perfil)
VALUES (1, 'Admin Agnello', 'admin@agnello.com',   crypt('admin123',   gen_salt('bf', 10)), 'Admin'),
       (1, 'Cliente Demo',  'cliente@agnello.com', crypt('cliente123', gen_salt('bf', 10)), 'Cliente')
ON CONFLICT DO NOTHING;
