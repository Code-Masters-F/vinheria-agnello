package br.com.agnellovinheria.config;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConfig {

    private static final HikariDataSource dataSource;

    static {
        try {
            // Carrega variáveis do arquivo .env
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            String dbUrl = dotenv.get("DB_URL");
            String dbUser = dotenv.get("DB_USER");
            String dbPassword = dotenv.get("DB_PASSWORD");
            String dbDriver = dotenv.get("DB_DRIVER", "org.postgresql.Driver");

            if (dbUrl == null || dbUrl.isBlank() ||
                    dbUser == null || dbUser.isBlank() ||
                    dbPassword == null || dbPassword.isBlank()) {
                throw new IllegalStateException("Missing required DB env vars in .env file: DB_URL, DB_USER, DB_PASSWORD");
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setDriverClassName(dbDriver);

            // Configurações otimizadas do Pool
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);

            // Configurações específicas para Banco de Dados e Schema
            config.addDataSourceProperty("ssl", "true");
            config.addDataSourceProperty("sslmode", "require");
            
            // Define o schema padrão do banco de dados (ex: 'public' para Supabase, 'vinheria_db' para AWS RDS)
            String dbSchema = dotenv.get("DB_SCHEMA", "public");
            config.addDataSourceProperty("currentSchema", dbSchema);

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
