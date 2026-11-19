package cr.ac.una.savora.data;

import org.springframework.data.jpa.domain.Specification;

public final class ReservaEspecificaciones {

    private ReservaEspecificaciones() {
    }

    public static Specification<Reserva> delCliente(Long clienteId) {
        return (root, query, cb) ->
                clienteId == null ? cb.conjunction() : cb.equal(root.get("cliente").get("id"), clienteId);
    }

    public static Specification<Reserva> conEstado(String estado) {
        return (root, query, cb) ->
                estado == null ? cb.conjunction() : cb.equal(root.get("estado"), estado);
    }

    /**
     * Reservas de los paquetes de un negocio. Sin negocio (null) no devuelve nada: es una
     * consulta de seguridad, nunca debe mostrar reservas de otros negocios por omisión.
     */
    public static Specification<Reserva> delNegocio(Long negocioId) {
        return (root, query, cb) -> negocioId == null
                ? cb.disjunction()
                : cb.equal(root.get("paqueteSorpresa").get("negocio").get("id"), negocioId);
    }

    /**
     * Trae paquete y cliente en la misma consulta (evita N+1 al armar el resumen). En la consulta
     * de conteo de la paginación no se aplica el fetch.
     */
    public static Specification<Reserva> conPaqueteYCliente() {
        return (root, query, cb) -> {
            if (query != null
                    && !Long.class.equals(query.getResultType())
                    && !long.class.equals(query.getResultType())) {
                root.fetch("paqueteSorpresa");
                root.fetch("cliente");
            }
            return cb.conjunction();
        };
    }
}