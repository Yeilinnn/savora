package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.ReservaResumen;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.data.Reserva;
import cr.ac.una.savora.data.ReservaEspecificaciones;
import cr.ac.una.savora.data.ReservaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaConsultaService {

    private final ReservaRepository reservaRepository;

    public ReservaConsultaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public Page<ReservaResumen> listar(Long clienteId, String estado, Pageable pageable) {
        Specification<Reserva> spec = Specification.allOf(
                ReservaEspecificaciones.delCliente(clienteId),
                ReservaEspecificaciones.conEstado(estado));
        return reservaRepository.findAll(spec, pageable).map(ReservaMapeador::aResumen);
    }

    @Transactional(readOnly = true)
    public ReservaResumen obtenerPorId(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
        return ReservaMapeador.aResumen(reserva);
    }
}
