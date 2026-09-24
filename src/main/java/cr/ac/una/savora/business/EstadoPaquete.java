package cr.ac.una.savora.business;

import cr.ac.una.savora.business.excepcion.TransicionEstadoInvalidaException;

import java.util.Map;
import java.util.Set;

public enum EstadoPaquete {
    DISPONIBLE("disponible"),
    RESERVADO("reservado"),
    RECOGIDO("recogido"),
    DONADO("donado"),
    PERDIDO("perdido"),
    AGOTADO("agotado");

    private final String valor;

    EstadoPaquete(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    private static final Map<EstadoPaquete, Set<EstadoPaquete>> TRANSICIONES = Map.of(
            DISPONIBLE, Set.of(RESERVADO, DONADO, PERDIDO, AGOTADO),
            RESERVADO, Set.of(RECOGIDO),
            RECOGIDO, Set.of(),
            DONADO, Set.of(),
            PERDIDO, Set.of(),
            AGOTADO, Set.of());

    public static EstadoPaquete desde(String valor) {
        for (EstadoPaquete estado : values()) {
            if (estado.valor.equals(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de paquete desconocido: " + valor);
    }

    public String transitarA(EstadoPaquete destino) {
        if (!TRANSICIONES.get(this).contains(destino)) {
            throw new TransicionEstadoInvalidaException(this, destino);
        }
        return destino.getValor();
    }
}