package br.com.agnellovinheria.service;

import br.com.agnellovinheria.config.DatabaseConfig;
import br.com.agnellovinheria.dao.HistoricoPontosDAO;
import br.com.agnellovinheria.model.HistoricoPontos;

import java.sql.Connection;
import java.sql.SQLException;

public class FidelidadeService {

    private final HistoricoPontosDAO historicoPontosDAO;

    public FidelidadeService() {
        this.historicoPontosDAO = new HistoricoPontosDAO();
    }

    public FidelidadeService(HistoricoPontosDAO historicoPontosDAO) {
        this.historicoPontosDAO = historicoPontosDAO;
    }

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    public void adicionarPontos(HistoricoPontos hist) throws Exception {
        try (Connection conn = getConnection()) {
            historicoPontosDAO.save(hist, conn);
        }
    }
}
