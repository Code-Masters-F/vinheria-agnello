package br.com.vinheiro.service;

import br.com.vinheiro.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class UsuarioAdminService {
    public UsuarioAdminService() {}

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }
}
