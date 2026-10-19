package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.ReservaResumen;
import cr.ac.una.savora.business.excepcion.PropiedadRecursoException;
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
    public Page<ReservaResumen> listarParaCliente(Long clienteIdAutenticado, String estado, Pageable pageable) {
        Specification<Reserva> spec = Specification.allOf(
                ReservaEspecificaciones.delCliente(clienteIdAutenticado),
                ReservaEspecificaciones.conEstado(estado));
        return reservaRepository.findAll(spec, pageable).map(ReservaMapeador::aResumen);
    }

    @Transactional(readOnly = true)
    public ReservaResumen obtenerPorIdParaCliente(Long id, Long clienteIdAutenticado) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
        if (!reserva.getCliente().getId().equals(clienteIdAutenticado)) {
            throw new PropiedadRecursoException("Reserva", id);
        }
        return ReservaMapeador.aResumen(reserva);
    }
}
