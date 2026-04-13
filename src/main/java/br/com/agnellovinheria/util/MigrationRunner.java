package br.com.agnellovinheria.util;

import br.com.agnellovinheria.config.DatabaseConfig;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

public class MigrationRunner {
    public static void main(String[] args) {
        String migrationPath = "C:\\Users\\Intel\\IdeaProjects\\vinheria-agnello\\migrations\\20240413_hardening_update.sql";
        System.out.println("Starting granular migration from: " + migrationPath);
        
        try {
            Dotenv.configure().directory("src").ignoreIfMissing().load();
            String content = new String(Files.readAllBytes(Paths.get(migrationPath)));
            String[] commands = content.split(";");
            
            try (Connection conn = DatabaseConfig.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("Processing " + commands.length + " commands...");
                
                for (String cmd : commands) {
                    String trimmed = cmd.trim();
                    if (trimmed.isEmpty()) continue;
                    
                    try {
                        System.out.println("Executing: " + (trimmed.length() > 50 ? trimmed.substring(0, 50) + "..." : trimmed));
                        stmt.execute(trimmed);
                    } catch (Exception e) {
                        if (e.getMessage().contains("already exists") || e.getMessage().contains("duplicate")) {
                            System.out.println(" - Skip: Constraint or column already exists.");
                        } else {
                            System.err.println(" ! Failed command: " + trimmed);
                            System.err.println(" ! Reason: " + e.getMessage());
                        }
                    }
                }
                System.out.println("Migration process completed!");
                
            } catch (ExceptionInInitializerError e) {
                System.err.println("Database initialization failed.");
                if (e.getCause() != null) System.err.println("Cause: " + e.getCause().getMessage());
            } catch (Exception e) {
                System.err.println("Connection error: " + e.getMessage());
            } finally {
                DatabaseConfig.shutdown();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
