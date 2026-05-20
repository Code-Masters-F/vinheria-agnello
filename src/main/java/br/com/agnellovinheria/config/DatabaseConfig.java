package br.com.agnellovinheria.config;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConfig {

    private static final HikariDataSource dataSource;

    /**
     * Reads an env variable from .env file first, then falls back to the OS
     * environment (used by Render, Docker, and other cloud platforms).
     */
    private static String getEnv(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value;
    }

    /**
     * Ensures the JDBC URL contains the sslmode=require parameter.
     * This is the most reliable way to enforce SSL with the PostgreSQL JDBC driver
     * in HikariCP's jdbcUrl (DriverManager) mode, avoiding DataSourceProperty issues.
     */
    private static String ensureSslInUrl(String url) {
        if (url == null) return null;
        if (url.contains("sslmode=")) return url; // Already set by the user
        return url.contains("?") ? url + "&sslmode=require" : url + "?sslmode=require";
    }

    static {
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            String dbUrl      = getEnv(dotenv, "DB_URL");
            String dbUser     = getEnv(dotenv, "DB_USER");
            String dbPassword = getEnv(dotenv, "DB_PASSWORD");
            String dbDriver   = getEnv(dotenv, "DB_DRIVER");
            String dbSchema   = getEnv(dotenv, "DB_SCHEMA");

            if (dbDriver == null || dbDriver.isBlank()) {
                dbDriver = "org.postgresql.Driver";
            }
            if (dbSchema == null || dbSchema.isBlank()) {
                dbSchema = "public";
            }

            if (dbUrl == null || dbUrl.isBlank() ||
                    dbUser == null || dbUser.isBlank() ||
                    dbPassword == null || dbPassword.isBlank()) {
                throw new IllegalStateException(
                    "Missing required DB env vars: DB_URL, DB_USER, DB_PASSWORD. " +
                    "Ensure they are set in the .env file (local) or in the " +
                    "Environment Variables panel (Render/Docker)."
                );
            }

            // Embed SSL directly in the URL — most reliable method in jdbcUrl (DriverManager) mode
            dbUrl = ensureSslInUrl(dbUrl);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setDriverClassName(dbDriver);

            // Pool sizing — keep small for Supabase free-tier connection limits
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(0);           // No eager connections on startup
            config.setConnectionTimeout(30000); // 30s to obtain a connection from the pool

            // Do NOT fail at startup if the DB isn't reachable yet (important inside Docker)
            config.setInitializationFailTimeout(-1);

            // Set the default search_path / schema
            config.setConnectionInitSql("SET search_path TO " + dbSchema);

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
