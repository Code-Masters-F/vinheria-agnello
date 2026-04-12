package br.com.agnellovinheria.servlet.admin;

import br.com.agnellovinheria.model.UsuarioAdmin;
import br.com.agnellovinheria.model.Vinheria;
import br.com.agnellovinheria.service.VinhoService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RelatoriosServlet (TDD — tests before implementation).
 */
@ExtendWith(MockitoExtension.class)
class RelatoriosServletTest {

    @Mock private VinhoService vinhoService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @InjectMocks
    private RelatoriosServlet servlet;

    private UsuarioAdmin admin;

    @BeforeEach
    void setUp() {
        Vinheria vinheria = new Vinheria();
        vinheria.setId(1L);

        admin = new UsuarioAdmin();
        admin.setId(1L);
        admin.setVinheria(vinheria);
    }

    @Test
    @DisplayName("Relatorios: GET should forward to relatorios.jsp with wine list")
    void doGet_shouldForwardWithData() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(vinhoService.listarDisponiveis(1L)).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/WEB-INF/views/admin/relatorios.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("currentPage"), eq("relatorios"));
        verify(request).setAttribute(eq("vinhos"), any());
        verify(dispatcher).forward(request, response);
    }
}
