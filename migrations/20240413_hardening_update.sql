-- Ensure missing tables exist
CREATE TABLE IF NOT EXISTS avaliacao_vinho (
    id          BIGSERIAL PRIMARY KEY,
    vinheria_id BIGINT NOT NULL,
    cliente_id  BIGINT NOT NULL,
    vinho_id    BIGINT NOT NULL,
    nota        SMALLINT NOT NULL CHECK (nota BETWEEN 1 AND 5),
    ocasiao     VARCHAR(60),
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS campanha (
    id              BIGSERIAL PRIMARY KEY,
    vinheria_id     BIGINT NOT NULL,
    nome            VARCHAR(150) NOT NULL,
    mensagem        TEXT NOT NULL,
    filtro_tipo     VARCHAR(60),
    canal           VARCHAR(20),
    status          VARCHAR(20),
    enviada_em      TIMESTAMP,
    criado_em       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- REPAIR: Standard ALTER commands (Runner skips if already exists)
ALTER TABLE item_pedido ADD COLUMN vinheria_id BIGINT;
ALTER TABLE avaliacao_vinho ADD COLUMN vinheria_id BIGINT;

-- PRE-REQUISITE: Parent tables must have UNIQUE constraints on (id, vinheria_id) for composite FKs
ALTER TABLE cliente ADD CONSTRAINT uq_cliente_tenant UNIQUE (id, vinheria_id);
ALTER TABLE vinho ADD CONSTRAINT uq_vinho_tenant UNIQUE (id, vinheria_id);
ALTER TABLE pedido ADD CONSTRAINT uq_pedido_tenant UNIQUE (id, vinheria_id);

-- 1. Add CHECK constraints for data integrity
ALTER TABLE vinho ADD CONSTRAINT check_preco_positivo CHECK (preco >= 0);
ALTER TABLE pedido ADD CONSTRAINT check_subtotal_positivo CHECK (subtotal >= 0);
ALTER TABLE pedido ADD CONSTRAINT check_total_positivo CHECK (total >= 0);
ALTER TABLE item_pedido ADD CONSTRAINT check_quantidade_positiva CHECK (quantidade > 0);
ALTER TABLE pagamento ADD CONSTRAINT check_valor_positivo CHECK (valor >= 0);

-- 2. Enforce NOT NULL for wine ratings
ALTER TABLE avaliacao_vinho ALTER COLUMN nota SET NOT NULL;

-- 3. Add Performance Indexes
CREATE INDEX IF NOT EXISTS idx_pagamento_pedido_id_v3 ON pagamento(pedido_id);
CREATE INDEX IF NOT EXISTS idx_avaliacao_vinho_cliente_vinho_v3 ON avaliacao_vinho(cliente_id, vinho_id);

-- 4. Update Foreign Keys to Composite (Tenant Consistency)
-- Item Pedido (Fixed Link)
ALTER TABLE item_pedido DROP CONSTRAINT IF EXISTS item_pedido_vinho_id_fkey;
ALTER TABLE item_pedido DROP CONSTRAINT IF EXISTS item_pedido_vinheria_id_fkey;
ALTER TABLE item_pedido ADD CONSTRAINT fk_item_pedido_vinho_v3 FOREIGN KEY (vinho_id, vinheria_id) REFERENCES vinho(id, vinheria_id) ON DELETE CASCADE;

-- (Remaining FKs)
ALTER TABLE pedido DROP CONSTRAINT IF EXISTS pedido_cliente_id_fkey;
ALTER TABLE pedido ADD CONSTRAINT fk_pedido_cliente_v3 FOREIGN KEY (cliente_id, vinheria_id) REFERENCES cliente(id, vinheria_id) ON DELETE CASCADE;
ALTER TABLE avaliacao_vinho DROP CONSTRAINT IF EXISTS avaliacao_vinho_cliente_id_fkey;
ALTER TABLE avaliacao_vinho ADD CONSTRAINT fk_avaliacao_cliente_v3 FOREIGN KEY (cliente_id, vinheria_id) REFERENCES cliente(id, vinheria_id) ON DELETE CASCADE;
ALTER TABLE avaliacao_vinho DROP CONSTRAINT IF EXISTS avaliacao_vinho_vinho_id_fkey;
ALTER TABLE avaliacao_vinho ADD CONSTRAINT fk_avaliacao_vinho_v3 FOREIGN KEY (vinho_id, vinheria_id) REFERENCES vinho(id, vinheria_id) ON DELETE CASCADE;
