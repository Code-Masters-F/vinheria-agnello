package br.com.vinheiro.dao;

import br.com.vinheiro.model.Cliente;
import br.com.vinheiro.model.Vinheria;
import br.com.vinheiro.model.enums.TipoCadastro;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Statement;
import java.util.Optional;

public class ClienteDAOTest extends BaseDAOTest {

    private ClienteDAO dao;
    private Vinheria testVinheria;

    @BeforeEach
    public void init() throws Exception {
        dao = new ClienteDAO();
        testVinheria = new Vinheria();
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO vinheria (nome, slug) VALUES ('Test Vinheria Cliente', 'test-vinheria-cliente')", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    testVinheria.setId(rs.getLong(1));
                }
            }
        }
    }

    @Test
    public void testSaveAndFindById() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setVinheriaId(testVinheria.getId());
        cliente.setNome("John Doe");
        cliente.setEmail("john@example.com");
        cliente.setTipoCadastro(TipoCadastro.online);
        cliente.setPontos(10);

        dao.save(cliente, connection);

        Assertions.assertTrue(cliente.getId() > 0);

        Optional<Cliente> found = dao.findById(cliente.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("John Doe", found.get().getNome());
        Assertions.assertEquals("john@example.com", found.get().getEmail());
        Assertions.assertEquals(10, found.get().getPontos());
    }
}
