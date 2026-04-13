package br.com.agnellovinheria.service;

import br.com.agnellovinheria.config.DatabaseConfig;
import br.com.agnellovinheria.dao.UsuarioAdminDAO;
import br.com.agnellovinheria.model.UsuarioAdmin;
import br.com.agnellovinheria.model.Vinheria;
import br.com.agnellovinheria.dao.VinheriaDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class UsuarioAdminService {
    private final UsuarioAdminDAO adminDAO;
    private final VinheriaDAO vinheriaDAO;

    public UsuarioAdminService() {
        this.adminDAO = new UsuarioAdminDAO();
        this.vinheriaDAO = new VinheriaDAO();
    }

    public Optional<UsuarioAdmin> autenticar(String email, String senha) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            Optional<UsuarioAdmin> optAdmin = adminDAO.findByEmail(email, conn);
            
            if (optAdmin.isPresent()) {
                UsuarioAdmin admin = optAdmin.get();
                // Verifica a senha usando o hash do banco
                if (BCrypt.checkpw(senha, admin.getSenhaHash())) {
                    // Carrega os dados completos da vinheria (o DAO só traz o ID por padrão)
                    if (admin.getVinheria() != null && admin.getVinheria().getId() != null) {
                        Optional<Vinheria> optVinheria = vinheriaDAO.findById(admin.getVinheria().getId());
                        optVinheria.ifPresent(admin::setVinheria);
                    }
                    return Optional.of(admin);
                }
            } else {
                // Dummy hash check to mitigate timing attacks by equalizing verification time
                BCrypt.checkpw(senha, "$2a$12$L7p6Y5y/C/yYVwF/YvYvYuG.v0K/Rk2YvYvYuG.v0K/Rk2YvYvYu");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar usuário", e);
        }
        return Optional.empty();
    }
}
