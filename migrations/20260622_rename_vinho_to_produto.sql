-- ============================================
-- Migration: 20260622_rename_vinho_to_produto
-- Description: Renomeia a tabela `vinho` para `produto` e cria uma
--              View `vinho` para retrocompatibilidade com o código existente.
-- Author: Antigravity Agent (feature/009-android-room)
-- Date: 2026-06-22
-- ============================================

BEGIN;

-- 1. Renomeia a tabela principal
ALTER TABLE vinho RENAME TO produto;

-- 2. Cria a view de retrocompatibilidade: todo o código existente que
--    usa SELECT/INSERT/UPDATE/DELETE na "tabela" vinho continuará funcionando
--    de forma transparente via esta view.
CREATE VIEW vinho AS SELECT * FROM produto;

-- 3. Recria o índice com o nome correto (o anterior era idx_vinho_vinheria)
DROP INDEX IF EXISTS idx_vinho_vinheria;
CREATE INDEX idx_produto_vinheria ON produto(vinheria_id);

-- 4. As Foreign Keys em PostgreSQL não precisam ser recriadas manualmente após
--    RENAME TABLE — as constraints internas continuam válidas.
--    Porém as FKs declarativas (em item_pedido e avaliacao_vinho) apontam para
--    o nome da tabela/sequência, que é atualizado automaticamente pelo RENAME.

COMMIT;
