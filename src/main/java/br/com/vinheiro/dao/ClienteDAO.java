package br.com.vinheiro.dao;

import br.com.vinheiro.model.Cliente;

import br.com.vinheiro.model.enums.TipoCadastro;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteDAO {
    private static final Logger LOGGER = Logger.getLogger(ClienteDAO.class.getName());

    public Optional<Cliente> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE id = ?";
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

    public void save(Cliente cliente, Connection conn) throws SQLException {
        String sql = "INSERT INTO cliente (vinheria_id, nome, email, whatsapp, cpf, endereco, senha_hash, tipo_cadastro, pontos) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, cliente.getVinheriaId());
            stmt.setString(2, cliente.getNome());
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getWhatsapp());
            stmt.setString(5, cliente.getCpf());
            stmt.setString(6, cliente.getEndereco());
            stmt.setString(7, cliente.getSenhaHash());
            if (cliente.getTipoCadastro() != null) {
                stmt.setString(8, cliente.getTipoCadastro().name());
            } else {
                stmt.setNull(8, Types.VARCHAR);
            }
            stmt.setInt(9, cliente.getPontos());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setId(rs.getLong(1));
                }
            }
        }
    }

    private Cliente mapRow(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("id"));
        
        cliente.setVinheriaId(rs.getLong("vinheria_id"));
        
        cliente.setNome(rs.getString("nome"));
        cliente.setEmail(rs.getString("email"));
        cliente.setWhatsapp(rs.getString("whatsapp"));
        cliente.setCpf(rs.getString("cpf"));
        cliente.setEndereco(rs.getString("endereco"));
        cliente.setSenhaHash(rs.getString("senha_hash"));
        
        String tipoStr = rs.getString("tipo_cadastro");
        if (tipoStr != null) {
            try {
                cliente.setTipoCadastro(TipoCadastro.valueOf(tipoStr.toUpperCase()));
            } catch(IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, "Invalid TipoCadastro value: {0} for cliente id: {1}", new Object[]{tipoStr, cliente.getId()});
            }
        }
        
        cliente.setPontos(rs.getInt("pontos"));
        return cliente;
    }
}
