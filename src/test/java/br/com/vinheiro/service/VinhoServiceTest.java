package br.com.vinheiro.service;

import br.com.vinheiro.dao.VinhoDAO;
import br.com.vinheiro.model.Vinheria;
import br.com.vinheiro.model.Vinho;
import br.com.vinheiro.model.enums.TipoVinho;
import br.com.vinheiro.service.exceptions.InvalidDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VinhoServiceTest {

    @Mock
    private VinhoDAO vinhoDAO;

    @Mock
    private Connection connection;

    private VinhoService vinhoService;

    @BeforeEach
    void setUp() {
        // Pass mock DAO directly. In actual runtime, it gets a real DAO.
        vinhoService = new VinhoService(vinhoDAO) {
            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
    }

    @Test
    void cadastrarVinho_WithZeroPrice_ShouldThrowInvalidDataException() {
        Vinho vinho = new Vinho();
        vinho.setPreco("0.00");
        vinho.setNome("Test Wine");
        vinho.setTipo(TipoVinho.tinto);

        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> {
            vinhoService.cadastrarVinho(vinho, 1L);
        });

        assertTrue(exception.getMessage().contains("price"));
        assertThrows(InvalidDataException.class, () -> {
            vinho.setPreco("-5.00");
            vinhoService.cadastrarVinho(vinho, 1L);
        });
    }

    @Test
    void atualizarEstoque_WhenEstoqueBecomesZero_ShouldDeactivateVinho() throws Exception {
        Vinho vinho = new Vinho();
        vinho.setId(1L);
        vinho.setEstoque(10);
        vinho.setAtivo(true);

        when(vinhoDAO.findById(eq(1L), any(Connection.class))).thenReturn(Optional.of(vinho));

        // When stock goes to 0
        vinhoService.atualizarEstoque(1L, 0);

        // Should update stock explicitly or implicitly save
        // But importantly, the object must be marked active = false
        assertFalse(vinho.isAtivo());
        // Since we update the object, the service should theoretically call save or update
        // We will assert save was called
        verify(vinhoDAO).save(vinho, connection);
    }
}
