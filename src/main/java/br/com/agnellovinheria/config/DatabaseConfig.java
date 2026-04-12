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
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            String dbUrl = dotenv.get("DB_URL");
            String dbUser = dotenv.get("DB_USER");
            String dbPassword = dotenv.get("DB_PASSWORD");
            String dbDriver = dotenv.get("DB_DRIVER", "org.postgresql.Driver");
            if (dbUrl == null || dbUrl.isBlank() ||
                    dbUser == null || dbUser.isBlank() ||
                    dbPassword == null || dbPassword.isBlank()) {
                throw new IllegalStateException("Missing required DB env vars: DB_URL, DB_USER, DB_PASSWORD");
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setDriverClassName(dbDriver);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Falha ao configurar o Pool HikariCP: " + e.getMessage());
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
