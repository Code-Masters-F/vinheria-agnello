-- Banco próprio do serviço de Catálogo (Database per Service).
-- Executar conectado ao banco catalogo_db.
-- A tabela vinheria pertence ao serviço de Identidade: aqui vinheria_id é apenas uma referência lógica
-- (sem FOREIGN KEY entre bancos de serviços diferentes).

CREATE TABLE IF NOT EXISTS produto (
    id              BIGSERIAL PRIMARY KEY,
    vinheria_id     BIGINT        NOT NULL,
    nome            VARCHAR(150)  NOT NULL,
    tipo            VARCHAR(20)   NOT NULL,
    uva             VARCHAR(100),
    pais            VARCHAR(80),
    regiao          VARCHAR(100),
    safra           INTEGER,
    preco           DECIMAL(10,2) NOT NULL CHECK (preco >= 0),
    descricao       TEXT,
    foto_url        VARCHAR(255),
    estoque         INT           DEFAULT 0,
    estoque_minimo  INT           DEFAULT 3,
    ativo           BOOLEAN       DEFAULT TRUE,
    criado_em       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_produto_vinheria ON produto (vinheria_id);

-- Dados de demonstração (vinheria 1 = Agnello; vinheria 2 = outra loja, para demonstrar o isolamento)
INSERT INTO produto (id, vinheria_id, nome, tipo, uva, pais, safra, preco, estoque, descricao)
VALUES (1, 1, 'Reserva do Porto Special', 'Tinto',  'Touriga Nacional', 'Portugal',  2018, 189.90, 24, 'Um vinho encorpado com notas de frutas negras e carvalho.'),
       (2, 1, 'Agnello Gran Selection',   'Branco', 'Chardonnay',       'Argentina', 2021, 125.00, 15, 'Fresco, equilibrado e ideal para acompanhar frutos do mar.'),
       (3, 1, 'Sunset Rose Premium',      'Rose',   'Grenache',         'França',    2022,  95.00, 42, 'Leve e refrescante, com aroma de morangos e flores brancas.'),
       (4, 2, 'Malbec da Outra Vinheria', 'Tinto',  'Malbec',           'Argentina', 2020, 110.00, 10, 'Produto de outra vinheria: o Admin da Agnello não pode alterá-lo.')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('produto', 'id'), (SELECT max(id) FROM produto));
