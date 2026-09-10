package cr.ac.una.savora.data;

import org.springframework.data.jpa.domain.Specification;

public final class ReservaEspecificaciones {

    private ReservaEspecificaciones() {
    }

    public static Specification<Reserva> delCliente(Long clienteId) {
        return (root, query, cb) -> cb.equal(root.get("cliente").get("id"), clienteId);
    }

    public static Specification<Reserva> conEstado(String estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }
}
