package br.com.agnellovinheria.model;

import br.com.agnellovinheria.model.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PedidoResumo — Lightweight DTO for the Admin Dashboard "Últimos Pedidos" table.
 *
 * <p>Carries only the fields needed for the summary view, populated via a single
 * JOIN query in PedidoDAO (no N+1). This avoids loading full entity graphs
 * (Pedido → Cliente → ItemPedido → Vinho) just to display a 5-row table.
 */
public class PedidoResumo {

    private Long id;
    private String clienteNome;
    private String vinhoNome;
    private LocalDateTime criadoEm;
    private BigDecimal total;
    private StatusPedido status;

    public PedidoResumo() {}

    public PedidoResumo(Long id, String clienteNome, String vinhoNome,
                        LocalDateTime criadoEm, BigDecimal total, StatusPedido status) {
        this.id = id;
        this.clienteNome = clienteNome;
        this.vinhoNome = vinhoNome;
        this.criadoEm = criadoEm;
        this.total = total;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public String getVinhoNome() { return vinhoNome; }
    public void setVinhoNome(String vinhoNome) { this.vinhoNome = vinhoNome; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }

    /**
     * Returns the formatted order ID in the pattern #AGN-XXXX.
     * Used by the JSP for display in the "Últimos Pedidos" table.
     */
    public String getIdFormatado() {
        return String.format("#AGN-%04d", id != null ? id : 0);
    }

    /**
     * Returns the first letter of the customer name (uppercased) for the avatar.
     * Falls back to "?" if the name is null or empty.
     */
    public String getClienteInicial() {
        if (clienteNome != null && !clienteNome.isBlank()) {
            return clienteNome.substring(0, 1).toUpperCase();
        }
        return "?";
    }
}
