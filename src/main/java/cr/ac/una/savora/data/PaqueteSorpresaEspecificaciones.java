package cr.ac.una.savora.data;

import org.springframework.data.jpa.domain.Specification;

public final class PaqueteSorpresaEspecificaciones {

    private PaqueteSorpresaEspecificaciones() {
    }

    public static Specification<PaqueteSorpresa> conEstado(String estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<PaqueteSorpresa> deCategoria(Long categoriaId) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId);
    }
}
