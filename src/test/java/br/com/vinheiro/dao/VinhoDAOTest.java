package br.com.vinheiro.dao;

import br.com.vinheiro.model.Vinho;
import br.com.vinheiro.model.Vinheria;
import br.com.vinheiro.model.enums.TipoVinho;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class VinhoDAOTest extends BaseDAOTest {

    private VinhoDAO dao;
    private Vinheria testVinheria;

    @BeforeEach
    public void init() throws Exception {
        dao = new VinhoDAO();
        testVinheria = new Vinheria();
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO vinheria (nome, slug) VALUES ('Test Vinheria', 'test-vinheria-vinho')", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    testVinheria.setId(rs.getLong(1));
                }
            }
        }
    }

    @Test
    public void testSaveAndFindById() throws Exception {
        Vinho vinho = new Vinho();
        vinho.setVinheria(testVinheria);
        vinho.setNome("Test Wine");
        vinho.setTipo(TipoVinho.tinto);
        vinho.setUva("Merlot");
        vinho.setPreco("59.9");
        vinho.setEstoque(10);
        vinho.setAtivo(true);

        dao.save(vinho, connection);

        Assertions.assertTrue(vinho.getId() > 0);

        Optional<Vinho> found = dao.findById(vinho.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Test Wine", found.get().getNome());
        Assertions.assertEquals(TipoVinho.tinto, found.get().getTipo());
    }

    @Test
    public void testFindByVinheriaId() throws Exception {
        Vinho vinho = new Vinho();
        vinho.setVinheria(testVinheria);
        vinho.setNome("Test Wine 2");
        vinho.setTipo(TipoVinho.branco);
        vinho.setUva("Chardonnay");
        vinho.setPreco("89.0");
        vinho.setEstoque(5);
        vinho.setAtivo(true);
        dao.save(vinho, connection);

        List<Vinho> list = dao.findByVinheriaId(testVinheria.getId(), connection);
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertTrue(list.stream().anyMatch(v -> v.getNome().equals("Test Wine 2")));
    }
}
