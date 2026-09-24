package cr.ac.una.savora.business.excepcion;

import cr.ac.una.savora.business.EstadoPaquete;

public class TransicionEstadoInvalidaException extends RuntimeException {
    public TransicionEstadoInvalidaException(EstadoPaquete origen, EstadoPaquete destino) {
        super("No se puede pasar el paquete de " + origen + " a " + destino);
    }
}