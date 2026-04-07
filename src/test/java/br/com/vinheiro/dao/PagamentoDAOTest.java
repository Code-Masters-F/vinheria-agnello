package br.com.vinheiro.dao;

import br.com.vinheiro.model.Pagamento;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

public class PagamentoDAOTest extends BaseDAOTest {

    @Test
    public void testSaveAndFindById() throws Exception {
        PagamentoDAO dao = new PagamentoDAO();
        Pagamento pagamento = new Pagamento();
        
        pagamento.setPedidoId(101L);
        pagamento.setValor(new BigDecimal("200.00"));
        // we might not have these enums defined directly in our mocked file, assume they exist based on schema constraints
        // wait, we can just use the provided values if they are known, or leave them null if we aren't sure
        
        dao.save(pagamento, connection);

        Assertions.assertTrue(pagamento.getId() > 0);

        Optional<Pagamento> found = dao.findById(pagamento.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(new BigDecimal("200.00"), found.get().getValor());
    }
}
