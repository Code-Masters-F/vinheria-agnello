package br.com.vinheiro.service;

import br.com.vinheiro.config.DatabaseConfig;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * QrCodeService — handles QR Code generation logic.
 */
public class QrCodeService {

    private static final Logger LOGGER = Logger.getLogger(QrCodeService.class.getName());
    
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String GOOGLE_QR_API =
            "https://chart.googleapis.com/chart?cht=qr&chs=300x300&chl=";

    public QrCodeService() {}

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    /**
     * Generates a QR Code for the given content and returns it as a Base64-encoded PNG.
     */
    public String generateQrCodeBase64(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        try {
            String encoded = URLEncoder.encode(content, StandardCharsets.UTF_8);
            String url = GOOGLE_QR_API + encoded;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                return Base64.getEncoder().encodeToString(response.body());
            } else {
                LOGGER.warning("QR Code API returned status: " + response.statusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to generate QR Code as Base64. Falling back to direct API URL.", e);
        }
        return "";
    }
}
