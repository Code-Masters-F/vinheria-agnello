package br.com.vinheiro.dao;

import br.com.vinheiro.model.Campanha;
import br.com.vinheiro.model.enums.StatusCampanha;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class CampanhaDAOTest extends BaseDAOTest {

    @Test
    public void testSaveAndFindById() throws Exception {
        CampanhaDAO dao = new CampanhaDAO();
        Campanha campanha = new Campanha(null, "Promoção Inverno", "tipo_novo_cliente");
        campanha.setVinheriaId(404L);
        campanha.setMensagem("Aproveite nossos vinhos de inverno com 20% OFF.");
        campanha.setStatus(StatusCampanha.rascunho);

        dao.save(campanha, connection);

        Assertions.assertNotNull(campanha);
        Assertions.assertNotNull(campanha.getId());
        Assertions.assertTrue(campanha.getId() > 0);

        Optional<Campanha> found = dao.findById(campanha.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Promoção Inverno", found.get().getNome());
        Assertions.assertEquals(StatusCampanha.rascunho, found.get().getStatus());
    }
}
