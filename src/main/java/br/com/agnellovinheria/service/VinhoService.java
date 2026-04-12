package br.com.agnellovinheria.service;

import br.com.agnellovinheria.config.DatabaseConfig;
import br.com.agnellovinheria.dao.VinhoDAO;
import br.com.agnellovinheria.model.Vinheria;
import br.com.agnellovinheria.model.Vinho;
import br.com.agnellovinheria.service.exceptions.InvalidDataException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VinhoService {

    private static final Logger LOGGER = Logger.getLogger(VinhoService.class.getName());
    private final VinhoDAO vinhoDAO;

    public VinhoService() {
        this.vinhoDAO = new VinhoDAO();
    }

    public VinhoService(VinhoDAO vinhoDAO) {
        this.vinhoDAO = vinhoDAO;
    }

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    /**
     * Cadastra um novo Vinho aplicando regras de negócio e validações.
     */
    public void cadastrarVinho(Vinho vinho, Long vinheriaId) throws InvalidDataException {
        validarPreco(vinho.getPreco());

        Vinheria v = new Vinheria();
        v.setId(vinheriaId);
        vinho.setVinheria(v);

        try (Connection conn = getConnection()) {
            vinhoDAO.save(vinho, conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving Vinho: " + e.getMessage(), e);
            throw new RuntimeException("Erro de banco ao cadastrar o vinho.", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }

    /**
     * Atualiza um Vinho existente após validar a propriedade (tenant safety).
     */
    public void atualizarVinho(Vinho vinho, Long vinheriaId) throws InvalidDataException {
        validarPreco(vinho.getPreco());

        try (Connection conn = getConnection()) {
            Optional<Vinho> existing = vinhoDAO.findById(vinho.getId(), conn);
            if (existing.isEmpty() || !existing.get().getVinheria().getId().equals(vinheriaId)) {
                throw new InvalidDataException("Vinho não encontrado ou não pertence a esta adega.");
            }

            Vinheria v = new Vinheria();
            v.setId(vinheriaId);
            vinho.setVinheria(v);

            vinhoDAO.update(vinho, conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating Vinho: " + e.getMessage(), e);
            throw new RuntimeException("Erro de banco ao atualizar o vinho.", e);
        }
    }

    private void validarPreco(String precoStr) throws InvalidDataException {
        if (precoStr == null || precoStr.isBlank()) {
            throw new InvalidDataException("O preço é obrigatório.");
        }
        try {
            BigDecimal preco = new BigDecimal(precoStr);
            if (preco.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidDataException("Invalid price: must be greater than zero.");
            }
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Formato de preço inválido.");
        }
    }

    /**
     * Atualiza o estoque de um vinho verificando a propriedade do tenant.
     */
    public void atualizarEstoque(Long vinhoId, Long vinheriaId, int novaQuantidade) throws Exception {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Vinho> optionalVinho = vinhoDAO.findById(vinhoId, conn);
                if (optionalVinho.isEmpty() || !optionalVinho.get().getVinheria().getId().equals(vinheriaId)) {
                    throw new InvalidDataException("Vinho não encontrado ou acesso não autorizado.");
                }

                Vinho vinho = optionalVinho.get();
                vinho.setEstoque(novaQuantidade);
                if (novaQuantidade <= 0) {
                    vinho.setAtivo(false);
                }
                
                vinhoDAO.update(vinho, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Optional<Vinho> findById(Long id, Long vinheriaId) {
        try (Connection conn = getConnection()) {
            Optional<Vinho> v = vinhoDAO.findById(id, conn);
            if (v.isPresent() && 
                v.get().getVinheria() != null && 
                v.get().getVinheria().getId() != null && 
                v.get().getVinheria().getId().equals(vinheriaId)) {
                return v;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding Vinho: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Vinho> listarDisponiveis(Long vinheriaId) {
        try (Connection conn = getConnection()) {
            return vinhoDAO.findByVinheriaId(vinheriaId, conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing Vinho: " + e.getMessage(), e);
            throw new RuntimeException("Database error listing items.");
        }
    }
}
