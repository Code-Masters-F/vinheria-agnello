package br.com.agnellovinheria.dao;

import br.com.agnellovinheria.model.UsuarioAdmin;
import br.com.agnellovinheria.model.Vinheria;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

public class UsuarioAdminDAOTest extends BaseDAOTest {

    private UsuarioAdminDAO dao;
    private Vinheria testVinheria;

    @BeforeEach
    public void init() throws Exception {
        dao = new UsuarioAdminDAO();
        testVinheria = new Vinheria();
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO vinheria (nome, slug) VALUES ('Test Vinheria', 'test-vinheria')", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    testVinheria.setId(rs.getLong(1));
                }
            }
        }
    }

    @Test
    public void testSaveAndFindById() throws Exception {
        UsuarioAdmin admin = new UsuarioAdmin();
        admin.setVinheria(testVinheria);
        admin.setNome("Admin Test");
        admin.setEmail("admin@test.com");
        admin.setSenhaHash("hashed123");
        admin.setCriadoEm(LocalDateTime.now());

        dao.save(admin, connection);

        Assertions.assertNotNull(admin.getId());
        Assertions.assertTrue(admin.getId() > 0, "ID should be generated");

        Optional<UsuarioAdmin> found = dao.findById(admin.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Admin Test", found.get().getNome());
        Assertions.assertEquals("admin@test.com", found.get().getEmail());
    }
    
    @Test
    public void testFindByEmail() throws Exception {
        UsuarioAdmin admin = new UsuarioAdmin();
        admin.setVinheria(testVinheria);
        admin.setNome("Admin Test 2");
        admin.setEmail("admin2@test.com");
        admin.setSenhaHash("hashed123");
        admin.setCriadoEm(LocalDateTime.now());

        dao.save(admin, connection);

        Optional<UsuarioAdmin> found = dao.findByEmail("admin2@test.com", connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Admin Test 2", found.get().getNome());
    }
}
