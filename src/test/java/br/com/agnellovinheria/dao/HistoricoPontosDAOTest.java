package br.com.agnellovinheria.dao;

import br.com.agnellovinheria.model.HistoricoPontos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Statement;
import java.util.Optional;

public class HistoricoPontosDAOTest extends BaseDAOTest {

    @Test
    public void testSaveAndFindById() throws Exception {
        // First, ensure a matching cliente exists for referential integrity
        long testClienteId;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO cliente (vinheria_id, nome, tipo_cadastro) VALUES (1, 'Test Client For History', 'comum')", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                rs.next();
                testClienteId = rs.getLong(1);
            }
        }

        HistoricoPontosDAO dao = new HistoricoPontosDAO();
        HistoricoPontos historico = new HistoricoPontos();
        historico.setClienteId(testClienteId);
        historico.setPontos(100);
        historico.setDescricao("Compra de vinho");
        
        dao.save(historico, connection);

        Assertions.assertNotNull(historico.getId());
        
        Optional<HistoricoPontos> found = dao.findById(historico.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(testClienteId, found.get().getClienteId());
        Assertions.assertEquals(100, found.get().getPontos());
        Assertions.assertEquals("Compra de vinho", found.get().getDescricao());
    }
}
