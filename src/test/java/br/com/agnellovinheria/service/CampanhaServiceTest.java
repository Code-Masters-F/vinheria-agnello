package br.com.agnellovinheria.service;

import br.com.agnellovinheria.dao.CampanhaDAO;
import br.com.agnellovinheria.model.Campanha;
import br.com.agnellovinheria.model.enums.StatusCampanha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampanhaServiceTest {

    @Mock
    private CampanhaDAO campanhaDAO;
    
    @Mock
    private Connection connection;

    private CampanhaService campanhaService;

    @BeforeEach
    void setUp() {
        campanhaService = new CampanhaService(campanhaDAO) {
            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
    }

    @Test
    void despacharCampanha_ShouldUpdateStatusToEnviada() throws Exception {
        Campanha c = new Campanha();
        c.setId(1L);
        c.setStatus(StatusCampanha.rascunho);

        when(campanhaDAO.findById(eq(1L), any())).thenReturn(Optional.of(c));

        campanhaService.despacharCampanha(1L);

        // Verification logic
        verify(campanhaDAO).atualizarStatus(eq(1L), eq(StatusCampanha.enviada), any(Connection.class));
    }
}
