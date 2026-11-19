package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.ResenaPromedio;
import cr.ac.una.savora.business.dto.ResenaResumen;
import cr.ac.una.savora.business.dto.ResenaSolicitud;
import cr.ac.una.savora.business.excepcion.PropiedadRecursoException;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.business.excepcion.ResenaDuplicadaException;
import cr.ac.una.savora.business.excepcion.ResenaNoPermitidaException;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.Reserva;
import cr.ac.una.savora.data.ReservaRepository;
import cr.ac.una.savora.data.Resena;
import cr.ac.una.savora.data.ResenaRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reseñas de paquetes recogidos (MongoDB). Reglas: solo el cliente dueño de la reserva puede
 * calificar, solo si la reserva fue recogida, y una sola reseña por reserva.
 */
@Service
public class ResenaService {

    private static final String ESTADO_RECOGIDO = "recogido";

    private final ResenaRepository resenaRepository;
    private final ReservaRepository reservaRepository;
    private final Clock clock;

    public ResenaService(ResenaRepository resenaRepository, ReservaRepository reservaRepository, Clock clock) {
        this.resenaRepository = resenaRepository;
        this.reservaRepository = reservaRepository;
        this.clock = clock;
    }

    @Transactional
    public ResenaResumen crear(Long reservaId, Long clienteIdAutenticado, ResenaSolicitud solicitud) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", reservaId));
        if (clienteIdAutenticado == null || !reserva.getCliente().getId().equals(clienteIdAutenticado)) {
            throw new PropiedadRecursoException("Reserva", reservaId);
        }
        if (!ESTADO_RECOGIDO.equals(reserva.getEstado())) {
            throw new ResenaNoPermitidaException(reservaId, reserva.getEstado());
        }
        if (resenaRepository.existsByReservaId(reservaId)) {
            throw new ResenaDuplicadaException(reservaId);
        }

        PaqueteSorpresa paquete = reserva.getPaqueteSorpresa();
        String comentario = solicitud.comentario() == null || solicitud.comentario().isBlank()
                ? null
                : solicitud.comentario().trim();
        Resena resena = new Resena(
                reservaId,
                paquete.getNegocio().getId(),
                paquete.getId(),
                clienteIdAutenticado,
                primerNombre(reserva.getCliente().getNombre()),
                solicitud.calificacion(),
                comentario,
                Instant.now(clock));
        try {
            return aResumen(resenaRepository.save(resena));
        } catch (DuplicateKeyException e) {
            // Dos solicitudes simultáneas: el índice único de MongoDB decide cuál gana.
            throw new ResenaDuplicadaException(reservaId);
        }
    }

    public Page<ResenaResumen> listarPorNegocio(Long negocioId, Pageable pageable) {
        return resenaRepository.findByNegocioId(negocioId, pageable).map(ResenaService::aResumen);
    }

    public ResenaPromedio resumenDeNegocio(Long negocioId) {
        return promediosPorNegocio(List.of(negocioId))
                .getOrDefault(negocioId, new ResenaPromedio(negocioId, null, 0));
    }

    /** Promedio y total por negocio; los negocios sin reseñas no aparecen en el mapa. */
    public Map<Long, ResenaPromedio> promediosPorNegocio(Collection<Long> negocioIds) {
        if (negocioIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, DoubleSummaryStatistics> porNegocio = resenaRepository.findByNegocioIdIn(negocioIds).stream()
                .collect(Collectors.groupingBy(
                        Resena::getNegocioId, Collectors.summarizingDouble(Resena::getCalificacion)));
        Map<Long, ResenaPromedio> resultado = new HashMap<>();
        porNegocio.forEach((id, stats) -> resultado.put(
                id, new ResenaPromedio(id, Math.round(stats.getAverage() * 10.0) / 10.0, (int) stats.getCount())));
        return resultado;
    }

    /** Ids de las reservas que el cliente ya calificó (para ocultar el botón "Calificar"). */
    public List<Long> reservasCalificadasPor(Long clienteId) {
        return resenaRepository.findByClienteId(clienteId).stream().map(Resena::getReservaId).toList();
    }

    private static String primerNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "Cliente";
        }
        return nombreCompleto.trim().split("\\s+")[0];
    }

    private static ResenaResumen aResumen(Resena resena) {
        return new ResenaResumen(
                resena.getId(),
                resena.getReservaId(),
                resena.getNegocioId(),
                resena.getPaqueteSorpresaId(),
                resena.getNombreCliente(),
                resena.getCalificacion(),
                resena.getComentario(),
                resena.getFecha());
    }
}