package cr.ac.una.savora.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "perfil_impacto")
@CompoundIndex(name = "uq_propietario_tipo", def = "{'propietarioId': 1, 'tipoPropietario': 1}", unique = true)
public class PerfilImpacto {

    @Id
    private String id;

    private String propietarioId;
    private TipoPropietario tipoPropietario;
    private BigDecimal kgRescatados = BigDecimal.ZERO;
    private int reservasRecogidas;
    private int reservasNoRecogidas;
    private int paquetesDonados;
    private int paquetesPerdidos;
    private List<Insignia> insignias = new ArrayList<>();

    public PerfilImpacto() {
    }

    public PerfilImpacto(String propietarioId, TipoPropietario tipoPropietario) {
        this.propietarioId = propietarioId;
        this.tipoPropietario = tipoPropietario;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(String propietarioId) {
        this.propietarioId = propietarioId;
    }

    public TipoPropietario getTipoPropietario() {
        return tipoPropietario;
    }

    public void setTipoPropietario(TipoPropietario tipoPropietario) {
        this.tipoPropietario = tipoPropietario;
    }

    public BigDecimal getKgRescatados() {
        return kgRescatados;
    }

    public void setKgRescatados(BigDecimal kgRescatados) {
        this.kgRescatados = kgRescatados;
    }

    public int getReservasRecogidas() {
        return reservasRecogidas;
    }

    public void setReservasRecogidas(int reservasRecogidas) {
        this.reservasRecogidas = reservasRecogidas;
    }

    public int getReservasNoRecogidas() {
        return reservasNoRecogidas;
    }

    public void setReservasNoRecogidas(int reservasNoRecogidas) {
        this.reservasNoRecogidas = reservasNoRecogidas;
    }

    public int getPaquetesDonados() {
        return paquetesDonados;
    }

    public void setPaquetesDonados(int paquetesDonados) {
        this.paquetesDonados = paquetesDonados;
    }

    public int getPaquetesPerdidos() {
        return paquetesPerdidos;
    }

    public void setPaquetesPerdidos(int paquetesPerdidos) {
        this.paquetesPerdidos = paquetesPerdidos;
    }

    public List<Insignia> getInsignias() {
        return insignias;
    }

    public void setInsignias(List<Insignia> insignias) {
        this.insignias = insignias;
    }
}