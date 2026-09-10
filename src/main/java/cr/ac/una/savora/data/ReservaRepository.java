package cr.ac.una.savora.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservaRepository extends RepositorioBase<Reserva, Long> {

    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.paqueteSorpresa p
            JOIN FETCH p.negocio
            WHERE r.cliente.id = :clienteId
              AND r.estado = 'pendiente'
            """)
    List<Reserva> findPendientesConPaquete(@Param("clienteId") Long clienteId);
}
