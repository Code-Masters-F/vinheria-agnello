package br.com.vinheiro.service;

import br.com.vinheiro.dao.VinhoDAO;
import br.com.vinheiro.model.Vinho;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecomendacaoServiceTest {

    @Mock
    private VinhoDAO vinhoDAO;

    @Mock
    private Connection connection;

    private RecomendacaoService recomendacaoService;

    @BeforeEach
    void setUp() {
        recomendacaoService = new RecomendacaoService(vinhoDAO) {
            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
    }

    @Test
    void obterRecomendacoes_WithFallback_ShouldReturnTop3Wines() throws Exception {
        Vinho v1 = new Vinho(); v1.setId(1L);
        Vinho v2 = new Vinho(); v2.setId(2L);
        Vinho v3 = new Vinho(); v3.setId(3L);
        Vinho v4 = new Vinho(); v4.setId(4L);

        when(vinhoDAO.findByVinheriaId(eq(1L), any(Connection.class)))
            .thenReturn(Arrays.asList(v1, v2, v3, v4));

        List<Vinho> recomendacoes = recomendacaoService.obterRecomendacoes(1L, "Jantar Romantico", null);

        assertEquals(3, recomendacoes.size());
    }
}
