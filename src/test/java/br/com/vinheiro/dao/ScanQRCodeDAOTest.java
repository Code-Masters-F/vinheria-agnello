package br.com.vinheiro.dao;

import br.com.vinheiro.model.ScanQRCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class ScanQRCodeDAOTest extends BaseDAOTest {

    @Test
    public void testSaveAndFindById() throws Exception {
        ScanQRCodeDAO dao = new ScanQRCodeDAO();
        ScanQRCode scan = new ScanQRCode(null, 15L, "Aniversário");
        scan.setFaixaPreco("Até R$ 100");
        scan.setConverteu(true);

        dao.save(scan, connection);

        Assertions.assertTrue(scan.getId() > 0);

        Optional<ScanQRCode> found = dao.findById(scan.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Aniversário", found.get().getOcasiao());
        Assertions.assertTrue(found.get().isConverteu());
    }
}
