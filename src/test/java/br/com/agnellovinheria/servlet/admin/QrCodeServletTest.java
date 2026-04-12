package br.com.agnellovinheria.servlet.admin;

import br.com.agnellovinheria.service.QrCodeService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import br.com.agnellovinheria.model.UsuarioAdmin;
import br.com.agnellovinheria.model.Vinheria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QrCodeServlet (TDD — tests before implementation).
 */
@ExtendWith(MockitoExtension.class)
class QrCodeServletTest {

    @Mock private QrCodeService qrCodeService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @InjectMocks
    private QrCodeServlet servlet;

    private UsuarioAdmin admin;

    @BeforeEach
    void setUp() {
        Vinheria vinheria = new Vinheria();
        vinheria.setId(1L);
        vinheria.setSlug("demo-vinheria");

        admin = new UsuarioAdmin();
        admin.setId(1L);
        admin.setVinheria(vinheria);
    }

    @Test
    @DisplayName("QrCode: GET should forward to qrcode.jsp")
    void doGet_shouldForwardToQrCodePage() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(request.getRequestDispatcher("/WEB-INF/views/admin/qrcode.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("currentPage"), eq("qrcode"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("QrCode: POST with valid occasion should set qrImageUrl attribute")
    void doPost_validOccasion_shouldSetQrImageUrl() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(request.getParameter("ocasiao")).thenReturn("Jantar em Família");
        when(qrCodeService.generateQrCodeBase64(anyString())).thenReturn("base64string==");
        when(request.getRequestDispatcher("/WEB-INF/views/admin/qrcode.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("qrImageBase64"), eq("base64string=="));
        verify(dispatcher).forward(request, response);
    }
}
