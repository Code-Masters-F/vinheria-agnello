package br.com.agnellovinheria.service;

import br.com.agnellovinheria.config.DatabaseConfig;
import br.com.agnellovinheria.dao.VinhoDAO;
import br.com.agnellovinheria.model.Vinho;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class RecomendacaoService {

    private final VinhoDAO vinhoDAO;

    public RecomendacaoService() {
        this.vinhoDAO = new VinhoDAO();
    }

    public RecomendacaoService(VinhoDAO vinhoDAO) {
        this.vinhoDAO = vinhoDAO;
    }

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    /**
     * OBTEM RECOMENDACOES DE VINHO.
     * @param vinheriaId ID vinheria
     * @param contexto e.g., "Jantar Romântico"
     * @param limite max results
     */
    public List<Vinho> obterRecomendacoes(Long vinheriaId, String contexto, Integer limite) {
        int max = (limite != null && limite > 0) ? limite : 3;

        try (Connection conn = getConnection()) {
            List<Vinho> todoEstoque = vinhoDAO.findByVinheriaId(vinheriaId, conn);

            // Simulating AI interaction or simple fallback
            // In the future this calls LLMApiClient, with timeout < 50ms mapped to fallback
            
            return todoEstoque.stream()
                .limit(max)
                .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar recomendações.", e);
        }
    }
}
