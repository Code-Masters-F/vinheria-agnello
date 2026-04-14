-- ============================================
-- VINHERIA AGNELLO - Test Data (DML)
-- ============================================

-- 1. Create the Vinheria Agnello Tenant
INSERT INTO vinheria (id, nome, slug, logo_url, cor_primaria, cor_secundaria) 
VALUES (1, 'Vinheria Agnello', 'agnello', 'https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=200', '#1e1b4b', '#e11d48')
ON CONFLICT (id) DO NOTHING;

-- 2. Create Admin User (Password: admin123)
-- Using a standard BCrypt hash for "admin123"
INSERT INTO usuario_admin (id, vinheria_id, nome, email, senha_hash)
VALUES (1, 1, 'Admin Agnello', 'admin@agnello.com', '$2a$10$C6L7LpgCdNIPs8gissy17eXMcWEYbgkg9pGUkB7cgQJHkpjeptv7q')
ON CONFLICT (email) DO UPDATE SET senha_hash = EXCLUDED.senha_hash;

-- 3. Catalog Data (Vinhos)
INSERT INTO vinho (id, vinheria_id, nome, tipo, uva, pais, safra, preco, estoque, descricao)
VALUES 
(1, 1, 'Reserva do Porto Special', 'Tinto', 'Touriga Nacional', 'Portugal', 2018, 189.90, 24, 'Um vinho encorpado com notas de frutas negras e carvalho.'),
(2, 1, 'Agnello Gran Selection', 'Branco', 'Chardonnay', 'Argentina', 2021, 125.00, 15, 'Fresco, equilibrado e ideal para acompanhar frutos do mar.'),
(3, 1, 'Sunset Rose Premium', 'Rose', 'Grenache', 'França', 2022, 95.00, 42, 'Leve e refrescante, com aroma de morangos e flores brancas.')
ON CONFLICT (id) DO NOTHING;

-- 4. Occasions
INSERT INTO vinho_ocasiao (vinho_id, ocasiao) VALUES 
(1, 'Jantar Romântico'), (1, 'Celebração'), (2, 'Almoço Executivo'), (3, 'Beira da Piscina')
ON CONFLICT (vinho_id, ocasiao) DO NOTHING;

-- 5. Test Customers
INSERT INTO cliente (id, vinheria_id, nome, email, whatsapp, cpf, tipo_cadastro, pontos)
VALUES 
(1, 1, 'João Silva', 'joao.silva@email.com', '11999998888', '123.456.789-00', 'completo', 150),
(2, 1, 'Maria Oliveira', 'maria.oliveira@email.com', '11888887777', '987.654.321-11', 'completo', 45)
ON CONFLICT (id) DO NOTHING;

-- 6. Test Orders (Pedidos)
INSERT INTO pedido (id, vinheria_id, cliente_id, status, tipo_entrega, subtotal, total)
VALUES 
(1, 1, 1, 'entregue', 'delivery', 314.90, 314.90),
(2, 1, 2, 'aguardando_pagamento', 'retirada', 125.00, 125.00)
ON CONFLICT (id) DO NOTHING;

-- 7. Order Items
INSERT INTO item_pedido (id, vinheria_id, pedido_id, vinho_id, quantidade, preco_unit)
VALUES 
(1, 1, 1, 1, 1, 189.90),
(2, 1, 1, 2, 1, 125.00),
(3, 1, 2, 2, 1, 125.00)
ON CONFLICT (id) DO NOTHING;

-- 8. Payments
INSERT INTO pagamento (id, pedido_id, metodo, status, valor)
VALUES 
(1, 1, 'cartao_credito', 'pago', 314.90)
ON CONFLICT (id) DO NOTHING;

-- 9. Reviews
INSERT INTO avaliacao_vinho (id, vinheria_id, cliente_id, vinho_id, nota, ocasiao)
VALUES 
(1, 1, 1, 1, 5, 'Comemoração de aniversário fantástica.')
ON CONFLICT (id) DO NOTHING;
