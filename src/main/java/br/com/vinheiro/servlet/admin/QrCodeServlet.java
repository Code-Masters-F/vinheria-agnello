package br.com.vinheiro.servlet.admin;

import br.com.vinheiro.model.UsuarioAdmin;
import br.com.vinheiro.model.Vinheria;
import br.com.vinheiro.service.QrCodeService;
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
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
            return;
        }

        UsuarioAdmin admin = (UsuarioAdmin) session.getAttribute("usuarioAdmin");
        Vinheria vinheria = admin.getVinheria();

        String ocasiao = req.getParameter("ocasiao");
        if (ocasiao == null || ocasiao.isBlank()) {
            req.setAttribute("errorMessage", "Selecione ou informe uma ocasião para gerar o QR Code.");
            req.setAttribute("currentPage", "qrcode");
            req.setAttribute("pageTitle",   "Gerador de QR Code");
            req.getRequestDispatcher("/WEB-INF/views/admin/qrcode.jsp").forward(req, resp);
            return;
        }

        try {
            // Build the sommelier URL the QR code will encode
            String baseUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                    + req.getContextPath();
            String ocasiaoEncoded = URLEncoder.encode(ocasiao, StandardCharsets.UTF_8);
            String targetUrl = baseUrl + "/sommelier?slug=" + vinheria.getSlug()
                    + "&ocasiao=" + ocasiaoEncoded;

            // Generate QR image via Google Charts API
            String qrApiUrl = "https://chart.googleapis.com/chart?cht=qr&chs=300x300&chl="
                    + URLEncoder.encode(targetUrl, StandardCharsets.UTF_8);

            String base64Image = qrCodeService.generateQrCodeBase64(targetUrl);

            req.setAttribute("qrImageBase64",  base64Image);
            req.setAttribute("qrApiUrl",       qrApiUrl);
            req.setAttribute("targetUrl",      targetUrl);
            req.setAttribute("ocasiao",        ocasiao);
            req.setAttribute("currentPage",    "qrcode");
            req.setAttribute("pageTitle",      "Gerador de QR Code");
            req.setAttribute("pageSubtitle",   "Crie QR Codes para cada ocasião da sua adega.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating QR code", e);
            req.setAttribute("errorMessage", "Erro ao gerar o QR Code. Tente novamente.");
            req.setAttribute("currentPage",  "qrcode");
            req.setAttribute("pageTitle",    "Gerador de QR Code");
        }

        req.getRequestDispatcher("/WEB-INF/views/admin/qrcode.jsp").forward(req, resp);
    }
}
