package br.com.vinheiro.service;

import br.com.vinheiro.config.DatabaseConfig;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;

/**
 * QrCodeService — handles QR Code generation logic.
 *
 * Currently uses the Google Charts QR Code API to generate QR images
 * and returns them as Base64-encoded PNG strings for embedding in JSP.
 *
 * Note: For production deployments, replace with a self-hosted solution
 * (e.g., the ZXing library) to avoid external API dependency.
 */
public class QrCodeService {

    private static final String GOOGLE_QR_API =
            "https://chart.googleapis.com/chart?cht=qr&chs=300x300&chl=";

    public QrCodeService() {}

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    /**
     * Generates a QR Code for the given content and returns it as a Base64-encoded PNG.
     * The returned value is suitable for embedding directly in an {@code <img src="...">} tag
     * as a data URL: {@code data:image/png;base64,<returned_value>}.
     *
     * @param content the text/URL to encode in the QR Code
     * @return Base64-encoded PNG string, or empty string on failure
     */
    public String generateQrCodeBase64(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        try {
            String encoded = URLEncoder.encode(content, StandardCharsets.UTF_8);
            String url = GOOGLE_QR_API + encoded;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = client.send(req, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                return Base64.getEncoder().encodeToString(response.body());
            }
        } catch (Exception e) {
            // Log but don't propagate — callers fall back to the API URL directly
        }
        return "";
    }
}
