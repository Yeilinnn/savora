package cr.ac.una.savora.data;

import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public final class PaqueteSorpresaEspecificaciones {

    private PaqueteSorpresaEspecificaciones() {
    }

    public static Specification<PaqueteSorpresa> conEstado(String estado) {
        return (root, query, cb) ->
                estado == null ? cb.conjunction() : cb.equal(root.get("estado"), estado);
    }

    public static Specification<PaqueteSorpresa> deCategoria(Long categoriaId) {
        return (root, query, cb) ->
                categoriaId == null ? cb.conjunction() : cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    public static Specification<PaqueteSorpresa> deNegocio(Long negocioId) {
        return (root, query, cb) ->
                negocioId == null ? cb.conjunction() : cb.equal(root.get("negocio").get("id"), negocioId);
    }

    /** Paquetes cuya hora límite ya pasó (candidatos a cierre automático). */
    public static Specification<PaqueteSorpresa> horaLimiteVencida(OffsetDateTime ahora) {
        return (root, query, cb) ->
                ahora == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("horaLimiteRecogida"), ahora);
    }
}