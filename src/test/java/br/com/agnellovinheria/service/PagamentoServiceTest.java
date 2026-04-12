package br.com.agnellovinheria.service;

import br.com.agnellovinheria.dao.PagamentoDAO;
import br.com.agnellovinheria.model.Pagamento;
import br.com.agnellovinheria.model.enums.StatusPagamento;
import br.com.agnellovinheria.service.exceptions.InvalidTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PagamentoServiceTest {

    @Mock
    private PagamentoDAO pagamentoDAO;
    
    @Mock
    private PedidoService pedidoService;
    
    @Mock
    private Connection connection;

    private PagamentoService pagamentoService;

    @BeforeEach
    void setUp() {
        pagamentoService = new PagamentoService(pagamentoDAO, pedidoService) {
            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
    }

    @Test
    void registrarCallback_WithInvalidTransition_ShouldThrowException() throws Exception {
        Pagamento pag = new Pagamento();
        pag.setStatus(StatusPagamento.aprovado);
        
        when(pagamentoDAO.findById(eq(1L), any(Connection.class))).thenReturn(Optional.of(pag));

        // It is invalid to transition from 'aprovado' back to 'pendente'
        assertThrows(InvalidTransitionException.class, () -> {
            pagamentoService.registrarCallbackGateway(1L, StatusPagamento.pendente, "gw_123");
        });
        
        verify(pagamentoDAO, never()).save(any(), any());
    }
}
