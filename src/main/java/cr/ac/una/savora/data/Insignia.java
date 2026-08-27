package cr.ac.una.savora.data;

import java.time.Instant;

public class Insignia {

    private CodigoInsignia codigo;
    private Instant fechaObtenida;

    public Insignia() {
    }

    public Insignia(CodigoInsignia codigo, Instant fechaObtenida) {
        this.codigo = codigo;
        this.fechaObtenida = fechaObtenida;
    }

    public CodigoInsignia getCodigo() {
        return codigo;
    }

    public void setCodigo(CodigoInsignia codigo) {
        this.codigo = codigo;
    }

    public Instant getFechaObtenida() {
        return fechaObtenida;
    }

    public void setFechaObtenida(Instant fechaObtenida) {
        this.fechaObtenida = fechaObtenida;
    }
}