package br.com.agnellovinheria.service;

import br.com.agnellovinheria.config.DatabaseConfig;
import br.com.agnellovinheria.dao.CampanhaDAO;
import br.com.agnellovinheria.model.Campanha;
import br.com.agnellovinheria.model.enums.StatusCampanha;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class CampanhaService {

    private final CampanhaDAO campanhaDAO;

    public CampanhaService() {
        this.campanhaDAO = new CampanhaDAO();
    }

    public CampanhaService(CampanhaDAO campanhaDAO) {
        this.campanhaDAO = campanhaDAO;
    }

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    public void despacharCampanha(Long id) throws Exception {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Campanha> opt = campanhaDAO.findById(id, conn);
                if (opt.isPresent()) {
                    Campanha c = opt.get();
                    if (c.getStatus() == StatusCampanha.rascunho) {
                        campanhaDAO.atualizarStatus(c.getId(), StatusCampanha.enviada, conn);
                        // Mocked Client Communication here in the future
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
