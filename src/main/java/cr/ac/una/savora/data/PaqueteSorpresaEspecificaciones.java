package cr.ac.una.savora.data;

import jakarta.persistence.criteria.JoinType;
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

    /**
     * Trae negocio y categoría con JOIN FETCH en la misma consulta (Lab 3: evita el N+1
     * al listar paquetes paginados). El "if" excluye el fetch de la consulta de conteo
     * que Spring Data ejecuta aparte para la paginación.
     */
    public static Specification<PaqueteSorpresa> conNegocioYCategoria() {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType()) {
                root.fetch("negocio", JoinType.LEFT);
                root.fetch("categoria", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }
}