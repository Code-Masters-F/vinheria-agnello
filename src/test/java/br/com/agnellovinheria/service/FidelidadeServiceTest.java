package br.com.agnellovinheria.service;

import br.com.agnellovinheria.dao.HistoricoPontosDAO;
import br.com.agnellovinheria.model.HistoricoPontos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class FidelidadeServiceTest {

    @Mock
    private HistoricoPontosDAO historicoPontosDAO;
    
    @Mock
    private Connection connection;

    private FidelidadeService fidelidadeService;

    @BeforeEach
    void setUp() {
        fidelidadeService = new FidelidadeService(historicoPontosDAO) {
            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
    }

    @Test
    void adicionarPontos_ShouldCallSave() throws Exception {
        HistoricoPontos hist = new HistoricoPontos();
        fidelidadeService.adicionarPontos(hist);

        verify(historicoPontosDAO).save(any(HistoricoPontos.class), any(Connection.class));
    }
}
