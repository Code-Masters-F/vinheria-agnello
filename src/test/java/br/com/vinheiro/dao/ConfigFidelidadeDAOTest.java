package br.com.vinheiro.dao;

import br.com.vinheiro.model.ConfigFidelidade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

public class ConfigFidelidadeDAOTest extends BaseDAOTest {

    @Test
    public void testSaveAndFindByVinheriaId() throws Exception {
        ConfigFidelidadeDAO dao = new ConfigFidelidadeDAO();
        ConfigFidelidade config = new ConfigFidelidade(777L);
        config.setPontosPorReal(new BigDecimal("1.5"));
        config.setRecompensas("10 pontos = 1 real");

        dao.save(config, connection);

        Optional<ConfigFidelidade> found = dao.findByVinheriaId(777L, connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(new BigDecimal("1.50"), found.get().getPontosPorReal().setScale(2));
        
        // Test update
        config.setRecompensas("15 pontos = 1 real");
        dao.save(config, connection);
        
        Optional<ConfigFidelidade> foundUpdated = dao.findByVinheriaId(777L, connection);
        Assertions.assertTrue(foundUpdated.isPresent());
        Assertions.assertEquals("15 pontos = 1 real", foundUpdated.get().getRecompensas());
    }
}
