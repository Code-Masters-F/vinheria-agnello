package br.com.vinheiro.dao;

import br.com.vinheiro.model.AvaliacaoVinho;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.util.Optional;

public class AvaliacaoVinhoDAOTest extends BaseDAOTest {

    @Test
    public void testSaveAndFindById() throws Exception {
        AvaliacaoVinhoDAO dao = new AvaliacaoVinhoDAO();
        AvaliacaoVinho avaliacao = new AvaliacaoVinho(null, 10L, 20L);
        avaliacao.setNota(5);
        avaliacao.setOcasiao("Jantar");
        
        dao.save(avaliacao, connection);

        Assertions.assertTrue(avaliacao.getId() > 0);

        Optional<AvaliacaoVinho> found = dao.findById(avaliacao.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(5, found.get().getNota());
        Assertions.assertEquals("Jantar", found.get().getOcasiao());
    }
}
