package br.com.vinheiro.servlet.admin;

import br.com.vinheiro.model.Vinho;
import br.com.vinheiro.model.UsuarioAdmin;
import br.com.vinheiro.model.Vinheria;
import br.com.vinheiro.service.VinhoService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CatalogoServlet (TDD — tests before implementation).
 */
@ExtendWith(MockitoExtension.class)
class CatalogoServletTest {

    @Mock private VinhoService vinhoService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @InjectMocks
    private CatalogoServlet servlet;

    private UsuarioAdmin admin;

    @BeforeEach
    void setUp() {
        Vinheria vinheria = new Vinheria();
        vinheria.setId(1L);

        admin = new UsuarioAdmin();
        admin.setId(1L);
        admin.setNome("Admin Teste");
        admin.setVinheria(vinheria);
    }

    @Test
    @DisplayName("Catalogo: GET should list wines from service and forward to catalogo.jsp")
    void doGet_shouldListWinesAndForward() throws Exception {
        List<Vinho> lista = Arrays.asList(new Vinho(), new Vinho());

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(request.getParameter("id")).thenReturn(null);
        when(vinhoService.listarDisponiveis(1L)).thenReturn(lista);
        when(request.getRequestDispatcher("/WEB-INF/views/admin/catalogo.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("vinhos"), eq(lista));
        verify(request).setAttribute(eq("currentPage"), eq("catalogo"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Catalogo: POST with empty nome should set error and re-forward")
    void doPost_emptyNome_shouldSetErrorAndForward() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(request.getParameter("nome")).thenReturn("");
        when(request.getParameter("preco")).thenReturn("50.00");
        when(request.getParameter("action")).thenReturn("create");
        when(vinhoService.listarDisponiveis(1L)).thenReturn(List.of());
        when(request.getRequestDispatcher("/WEB-INF/views/admin/catalogo.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), anyString());
        verify(dispatcher).forward(request, response);
        // Service should NOT be called to save invalid data
        verify(vinhoService, never()).cadastrarVinho(any(), anyLong());
    }

    @Test
    @DisplayName("Catalogo: POST with valid data should redirect after create (PRG)")
    void doPost_validData_shouldRedirectAfterCreate() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(request.getParameter("nome")).thenReturn("Malbec Reserva");
        when(request.getParameter("preco")).thenReturn("89.90");
        when(request.getParameter("tipo")).thenReturn("TINTO");
        when(request.getParameter("estoque")).thenReturn("20");
        when(request.getParameter("action")).thenReturn("create");
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);

        verify(vinhoService).cadastrarVinho(any(Vinho.class), eq(1L));
        verify(response).sendRedirect(anyString());
    }
}
