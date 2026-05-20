package br.com.agnellovinheria.servlet;

import br.com.agnellovinheria.config.DatabaseConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Diagnostic endpoint — accessible at /health.
 * Tests DB connectivity and reports the result.
 * REMOVE or RESTRICT this servlet before going to a public production environment.
 */
@WebServlet("/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("=== Vinheria Agnello — Health Check ===");
        out.println();

        // 1. Check environment variables
        out.println("[1] Variáveis de ambiente:");
        out.println("  DB_URL      = " + maskUrl(System.getenv("DB_URL")));
        out.println("  DB_USER     = " + System.getenv("DB_USER"));
        out.println("  DB_PASSWORD = " + (System.getenv("DB_PASSWORD") != null ? "***SET***" : "NULL (MISSING!)"));
        out.println("  DB_SCHEMA   = " + System.getenv("DB_SCHEMA"));
        out.println();

        // 2. Test database connection
        out.println("[2] Teste de conexão com o banco:");
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT current_user, current_database(), now()")) {

            if (rs.next()) {
                out.println("  STATUS    = OK ✓");
                out.println("  Usuário   = " + rs.getString(1));
                out.println("  Database  = " + rs.getString(2));
                out.println("  Hora DB   = " + rs.getString(3));
            }
            response.setStatus(200);

        } catch (Throwable t) {
            response.setStatus(500);
            out.println("  STATUS    = FALHA ✗");
            out.println("  Tipo      = " + t.getClass().getName());
            out.println("  Mensagem  = " + t.getMessage());
            if (t.getCause() != null) {
                out.println("  Causa     = " + t.getCause().getClass().getName() + ": " + t.getCause().getMessage());
                if (t.getCause().getCause() != null) {
                    out.println("  Causa 2   = " + t.getCause().getCause().getMessage());
                }
            }
            out.println();
            out.println("  Stack Trace:");
            t.printStackTrace(out);

            // Also log to Render's log stream
            System.err.println("[HEALTH] Falha na conexão com o banco: " + t.getMessage());
            t.printStackTrace(System.err);
        }

        out.println();
        out.println("=== Fim do Health Check ===");
    }

    private String maskUrl(String url) {
        if (url == null) return "NULL (MISSING!)";
        // Hide password in URL if present
        return url.replaceAll(":[^:@/]+@", ":***@");
    }
}
