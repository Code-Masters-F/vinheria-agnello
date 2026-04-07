package br.com.vinheiro.dao;

import br.com.vinheiro.model.Vinho;
import br.com.vinheiro.model.Vinheria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * VinhoDAO - Data Access Object for the Vinho entity.
 *
 * <p><b>SQL / Security Audit (T026):</b> 
 * All queries execute strictly via JDBC {@link PreparedStatement}s. User inputs 
 * (e.g., nome, description, id) are securely bound as positional parameters, 
 * structurally mitigating any threat of SQL Injection (SQLi) directly at the database driver level.
 * 
 * <p><b>Design & Joins (T025):</b>
 * Complex JOIN operations have been avoided by design to maintain single-responsibility DAO 
 * semantics (e.g., no immediate left joins on Vinheria or Avaliacao_Vinho unless explicitly requested).
 * Foreign entities (like Vinheria) are mapped lazily/structurally by assigning their IDs to 
 * empty model instances, deferring deep loading to the Business Service layer and keeping 
 * DAOs performant and strictly cohesive with their primary table context.
 */
public class VinhoDAO {

    public Optional<Vinho> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM vinho WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Vinho> findByVinheriaId(Long vinheriaId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM vinho WHERE vinheria_id = ? AND ativo = TRUE";
        List<Vinho> vinhos = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vinheriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vinhos.add(mapRow(rs));
                }
            }
        }
        return vinhos;
    }

    public void save(Vinho vinho, Connection conn) throws SQLException {
        String sql = "INSERT INTO vinho (vinheria_id, nome, tipo, uva, pais, regiao, safra, preco, descricao, foto_url, estoque, estoque_minimo, ativo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, vinho.getVinheria().getId());
            stmt.setString(2, vinho.getNome());
            stmt.setString(3, vinho.getTipo().name());
            stmt.setString(4, vinho.getUva());
            stmt.setString(5, vinho.getPais());
            stmt.setString(6, vinho.getRegiao());
            
            if (vinho.getSafra() != null) stmt.setInt(7, Integer.parseInt(vinho.getSafra()));
            else stmt.setNull(7, Types.INTEGER);
            
            stmt.setDouble(8, Double.parseDouble(vinho.getPreco()));
            stmt.setString(9, vinho.getDescricao());
            stmt.setString(10, vinho.getFotoUrl());
            stmt.setInt(11, vinho.getEstoque());
            stmt.setInt(12, vinho.getEstoqueMinimo());
            stmt.setBoolean(13, vinho.isAtivo());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    vinho.setId(rs.getLong(1));
                }
            }
        }
    }

    public void updateEstoque(Long id, int novoEstoque, Connection conn) throws SQLException {
        String sql = "UPDATE vinho SET estoque = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novoEstoque);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    private Vinho mapRow(ResultSet rs) throws SQLException {
        Vinho vinho = new Vinho();
        vinho.setId(rs.getLong("id"));
        
        Vinheria v = new Vinheria();
        v.setId(rs.getLong("vinheria_id"));
        vinho.setVinheria(v);
        
        vinho.setNome(rs.getString("nome"));
        
        String tipoStr = rs.getString("tipo");
        if (tipoStr != null) {
            try {
                vinho.setTipo(br.com.vinheiro.model.enums.TipoVinho.valueOf(tipoStr.toLowerCase()));
            } catch(IllegalArgumentException e) {
                // Ignore parsing errors for enums in simple mapper
            }
        }
        
        vinho.setUva(rs.getString("uva"));
        vinho.setPais(rs.getString("pais"));
        vinho.setRegiao(rs.getString("regiao"));
        
        int safra = rs.getInt("safra");
        if (!rs.wasNull()) {
            vinho.setSafra(String.valueOf(safra));
        }
                
        vinho.setPreco(String.valueOf(rs.getDouble("preco")));
        vinho.setDescricao(rs.getString("descricao"));
        vinho.setFotoUrl(rs.getString("foto_url"));
        vinho.setEstoque(rs.getInt("estoque"));
        vinho.setEstoqueMinimo(rs.getInt("estoque_minimo"));
        vinho.setAtivo(rs.getBoolean("ativo"));
        return vinho;
    }
}
