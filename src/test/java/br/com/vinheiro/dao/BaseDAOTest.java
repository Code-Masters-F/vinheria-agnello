package br.com.vinheiro.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public abstract class BaseDAOTest {

    protected Connection connection;

    @BeforeEach
    public void setUpConnection() throws Exception {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("jdbc.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                throw new RuntimeException("jdbc.properties not found");
            }
        }

        Class.forName(props.getProperty("db.driver"));
        connection = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        );

        // Optionally, run schema creation here if needed
        createSchema();
    }

    @AfterEach
    public void tearDownConnection() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP ALL OBJECTS");
            }
            connection.close();
        }
    }

    private void createSchema() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            // Simplified schema creation for testing.
            // Note: Foreign key constraints are intentionally omitted to simplify the in-memory/test schema.
            // Tests do not validate referential integrity here; these should be covered by dedicated integration tests.
            stmt.execute("CREATE TABLE IF NOT EXISTS vinheria (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(100) NOT NULL, " +
                    "slug VARCHAR(60) UNIQUE NOT NULL, " +
                    "logo_url VARCHAR(255), " +
                    "cor_primaria VARCHAR(7), " +
                    "cor_secundaria VARCHAR(7), " +
                    "ativo BOOLEAN DEFAULT TRUE " +
                    ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS usuario_admin (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "vinheria_id BIGINT NOT NULL, " +
                    "nome VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(150) UNIQUE NOT NULL, " +
                    "senha_hash VARCHAR(255) NOT NULL, " +
                    "criado_em DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS vinho (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "vinheria_id BIGINT NOT NULL, " +
                    "nome VARCHAR(150) NOT NULL, " +
                    "tipo VARCHAR(50) NOT NULL, " +
                    "uva VARCHAR(100), " +
                    "pais VARCHAR(80), " +
                    "regiao VARCHAR(100), " +
                    "safra INT, " +
                    "preco DECIMAL(10,2) NOT NULL, " +
                    "descricao TEXT, " +
                    "foto_url VARCHAR(255), " +
                    "estoque INT DEFAULT 0, " +
                    "estoque_minimo INT DEFAULT 3, " +
                    "ativo BOOLEAN DEFAULT TRUE" +
                    ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS cliente (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "vinheria_id BIGINT NOT NULL, " +
                    "nome VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(150), " +
                    "whatsapp VARCHAR(20), " +
                    "cpf VARCHAR(14), " +
                    "endereco TEXT, " +
                    "senha_hash VARCHAR(255), " +
                    "tipo_cadastro VARCHAR(50) NOT NULL, " +
                    "pontos INT DEFAULT 0" +
                    ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS pedido (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "vinheria_id BIGINT NOT NULL, " +
                    "cliente_id BIGINT, " +
                    "status VARCHAR(50) DEFAULT 'aguardando_pagamento', " +
                    "tipo_entrega VARCHAR(50) NOT NULL, " +
                    "subtotal DECIMAL(10,2) NOT NULL, " +
                    "total DECIMAL(10,2) NOT NULL, " +
                    "endereco_entrega TEXT, " +
                    "criado_em DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "atualizado_em DATETIME" +
                    ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS item_pedido (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "pedido_id BIGINT NOT NULL, " +
                    "vinho_id BIGINT NOT NULL, " +
                    "quantidade INT NOT NULL, " +
                    "preco_unit DECIMAL(10,2) NOT NULL" +
                    ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS pagamento (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "pedido_id BIGINT NOT NULL, " +
                    "metodo VARCHAR(50), " +
                    "status VARCHAR(50), " +
                    "valor DECIMAL(10,2) NOT NULL, " +
                    "gateway_id VARCHAR(255), " +
                    "criado_em DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS avaliacao_vinho (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "cliente_id BIGINT NOT NULL, " +
                    "vinho_id BIGINT NOT NULL, " +
                    "nota INT, " +
                    "ocasiao VARCHAR(50), " +
                    "criado_em DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS campanha (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "vinheria_id BIGINT NOT NULL, " +
                    "nome VARCHAR(255) NOT NULL, " +
                    "mensagem TEXT, " +
                    "filtro_tipo VARCHAR(50), " +
                    "canal VARCHAR(50), " +
                    "status VARCHAR(50), " +
                    "enviada_em DATETIME, " +
                    "criado_em DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS config_fidelidade (" +
                    "vinheria_id BIGINT PRIMARY KEY, " +
                    "pontos_por_real DECIMAL(10,2), " +
                    "validade_dias INT, " +
                    "recompensas TEXT" +
                    ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS historico_pontos (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "cliente_id BIGINT NOT NULL, " +
                    "pedido_id BIGINT, " +
                    "pontos INT NOT NULL, " +
                    "descricao VARCHAR(255), " +
                    "criado_em DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS scan_qrcode (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "vinheria_id BIGINT NOT NULL, " +
                    "ocasiao VARCHAR(50), " +
                    "faixa_preco VARCHAR(50), " +
                    "converteu BOOLEAN DEFAULT FALSE, " +
                    "criado_em DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");
        }
    }
}
