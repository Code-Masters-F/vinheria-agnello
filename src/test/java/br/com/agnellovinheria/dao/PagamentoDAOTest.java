package br.com.agnellovinheria.dao;

import br.com.agnellovinheria.model.Pagamento;
import br.com.agnellovinheria.model.enums.MetodoPagamento;
import br.com.agnellovinheria.model.enums.StatusPagamento;
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
        pagamento.setMetodo(MetodoPagamento.cartao_credito);
        pagamento.setStatus(StatusPagamento.aprovado);
        
        dao.save(pagamento, connection);

        Assertions.assertNotNull(pagamento);
        Assertions.assertNotNull(pagamento.getId());
        Assertions.assertTrue(pagamento.getId() > 0);

        Optional<Pagamento> found = dao.findById(pagamento.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(new BigDecimal("200.00"), found.get().getValor());
    }
}
