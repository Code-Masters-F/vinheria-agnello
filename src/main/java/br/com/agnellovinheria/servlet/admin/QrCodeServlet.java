package br.com.agnellovinheria.servlet.admin;

import br.com.agnellovinheria.model.UsuarioAdmin;
import br.com.agnellovinheria.model.Vinheria;
import br.com.agnellovinheria.service.QrCodeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * QrCodeServlet — Controller for the Admin QR Code Generator page.
 *
 * GET  /admin/qrcode → renders the QR form
 * POST /admin/qrcode → generates a QR Code for the given occasion
 *
 * Uses the Google Charts QR Code API to generate the image.
 * Note: In production, replace with a local QR library (e.g., ZXing).
 */
@WebServlet(name = "QrCodeServlet", urlPatterns = "/admin/qrcode")
public class QrCodeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(QrCodeServlet.class.getName());

    private QrCodeService qrCodeService;

    @Override
    public void init() throws ServletException {
        this.qrCodeService = new QrCodeService();
    }

    /** Package-private for test injection. */
    QrCodeServlet(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    public QrCodeServlet() {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
            return;
        }

        req.setAttribute("currentPage",  "qrcode");
        req.setAttribute("pageTitle",    "Gerador de QR Code");
        req.setAttribute("pageSubtitle", "Crie QR Codes para cada ocasião da sua adega.");

        req.getRequestDispatcher("/WEB-INF/views/admin/qrcode.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        UsuarioAdmin admin = (UsuarioAdmin) session.getAttribute("usuarioAdmin");
        if (admin == null || admin.getVinheria() == null) {
             resp.sendRedirect(req.getContextPath() + "/auth/login");
             return;
        }
        Vinheria vinheria = admin.getVinheria();

        String ocasiao = req.getParameter("ocasiao");
        if (ocasiao == null || ocasiao.isBlank()) {
            req.setAttribute("errorMessage", "Selecione ou informe uma ocasião para gerar o QR Code.");
            req.setAttribute("currentPage", "qrcode");
            req.setAttribute("pageTitle",   "Gerador de QR Code");
            req.setAttribute("pageSubtitle", "Crie QR Codes para cada ocasião da sua adega.");
            req.getRequestDispatcher("/WEB-INF/views/admin/qrcode.jsp").forward(req, resp);
            return;
        }

        try {
            // Build the sommelier URL encoding
            String baseUrl = getPublicBaseUrl(req);
            String slug = vinheria.getSlug();
            if (slug == null || slug.isBlank()) {
                throw new Exception("Vinheria sem slug definido. Não é possível gerar QR Code.");
            }
            String slugEncoded = URLEncoder.encode(slug, StandardCharsets.UTF_8);
            String ocasiaoEncoded = URLEncoder.encode(ocasiao, StandardCharsets.UTF_8);
            String targetUrl = baseUrl + "/sommelier?slug=" + slugEncoded
                    + "&ocasiao=" + ocasiaoEncoded;

            // Generate QR image via service (which may return base64)
            String base64Image = qrCodeService.generateQrCodeBase64(targetUrl);

            // Fallback for UI visualization (Google Charts URL)
            String qrApiUrl = "https://chart.googleapis.com/chart?cht=qr&chs=300x300&chl="
                    + URLEncoder.encode(targetUrl, StandardCharsets.UTF_8);

            req.setAttribute("qrImageBase64",  base64Image);
            req.setAttribute("qrApiUrl",       qrApiUrl);
            req.setAttribute("targetUrl",      targetUrl);
            req.setAttribute("ocasiao",        ocasiao);
            req.setAttribute("currentPage",    "qrcode");
            req.setAttribute("pageTitle",      "Gerador de QR Code");
            req.setAttribute("pageSubtitle",   "Crie QR Codes para cada ocasião da sua adega.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating QR code", e);
            req.setAttribute("errorMessage", "Erro ao gerar o QR Code: " + e.getMessage());
            req.setAttribute("currentPage",  "qrcode");
            req.setAttribute("pageTitle",    "Gerador de QR Code");
            req.setAttribute("pageSubtitle", "Crie QR Codes para cada ocasião da sua adega.");
        }

        req.getRequestDispatcher("/WEB-INF/views/admin/qrcode.jsp").forward(req, resp);
    }

    /**
     * Derives the public base URL, favoring PUBLIC_BASE_URL env var,
     * otherwise falls back to normalized request headers/properties.
     */
    private String getPublicBaseUrl(HttpServletRequest req) {
        String envBase = System.getenv("PUBLIC_BASE_URL");
        if (envBase != null && !envBase.isBlank()) {
            return envBase.endsWith("/") ? envBase.substring(0, envBase.length() - 1) : envBase;
        }

        // Fallback: Derivation from request with proxy header awareness
        String proto = req.getHeader("X-Forwarded-Proto");
        if (proto == null) proto = req.getScheme();

        String hostHeader = req.getHeader("X-Forwarded-Host");
        String host = (hostHeader != null && !hostHeader.isBlank()) ? hostHeader.split(",")[0].trim() : req.getServerName();

        // Handle case where host string might already contain a port
        String[] hostParts = host.split(":");
        String actualHost = hostParts[0];

        String portHeader = req.getHeader("X-Forwarded-Port");
        String port = (portHeader != null && !portHeader.isBlank()) ? portHeader : String.valueOf(req.getServerPort());
        
        // If host header already included a port, override the port variable
        if (hostParts.length > 1) {
            port = hostParts[1];
        }

        String context = req.getContextPath();
        
        StringBuilder sb = new StringBuilder();
        sb.append(proto).append("://").append(actualHost);
        
        boolean isDefaultPort = ("http".equals(proto) && "80".equals(port)) || ("https".equals(proto) && "443".equals(port));
        if (!isDefaultPort) {
            sb.append(":").append(port);
        }
        
        sb.append(context);
        return sb.toString();
    }
}
