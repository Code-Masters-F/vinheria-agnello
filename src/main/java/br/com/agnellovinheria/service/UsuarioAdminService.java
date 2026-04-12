package br.com.agnellovinheria.service;

import br.com.agnellovinheria.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class UsuarioAdminService {
    public UsuarioAdminService() {}

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }
}
