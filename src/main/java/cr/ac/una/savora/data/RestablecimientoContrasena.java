package cr.ac.una.savora.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Enlace de un solo uso para restablecer la contraseña. Solo se guarda el hash (SHA-256) del
 * token: si alguien viera la base de datos, no podría usar los enlaces.
 */
@Entity
@Table(name = "restablecimiento_contrasena")
public class RestablecimientoContrasena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expira_en", nullable = false)
    private OffsetDateTime expiraEn;

    @Column(name = "usado_en")
    private OffsetDateTime usadoEn;

    protected RestablecimientoContrasena() {
    }

    public RestablecimientoContrasena(Usuario usuario, String tokenHash, OffsetDateTime expiraEn) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.expiraEn = expiraEn;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiraEn() {
        return expiraEn;
    }

    public OffsetDateTime getUsadoEn() {
        return usadoEn;
    }

    public void marcarUsado(OffsetDateTime momento) {
        this.usadoEn = momento;
    }

    public boolean estaVigente(OffsetDateTime ahora) {
        return usadoEn == null && expiraEn.isAfter(ahora);
    }
}