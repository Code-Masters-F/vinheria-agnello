package br.com.vinheiro.dao;

import br.com.vinheiro.model.HistoricoPontos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class HistoricoPontosDAOTest extends BaseDAOTest {

    @Test
    public void testSaveAndFindById() throws Exception {
        HistoricoPontosDAO dao = new HistoricoPontosDAO();
        HistoricoPontos historico = new HistoricoPontos(null, 50L, 100, "Compra de vinhos de inverno");

        dao.save(historico, connection);

        Assertions.assertTrue(historico.getId() > 0);

        Optional<HistoricoPontos> found = dao.findById(historico.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(100, found.get().getPontos());
        Assertions.assertEquals("Compra de vinhos de inverno", found.get().getDescricao());
    }
}
