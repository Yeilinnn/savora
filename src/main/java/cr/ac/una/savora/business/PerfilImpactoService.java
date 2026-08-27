package cr.ac.una.savora.business;

import cr.ac.una.savora.data.CodigoInsignia;
import cr.ac.una.savora.data.Insignia;
import cr.ac.una.savora.data.PerfilImpacto;
import cr.ac.una.savora.data.PerfilImpactoRepository;
import cr.ac.una.savora.data.TipoPropietario;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PerfilImpactoService {

    private static final int LIMITE_RESERVAS_BASE = 3;
    private static final int LIMITE_RESERVAS_CONFIABLE = 5;
    private static final int UMBRAL_RECOGIDAS_GUARDIAN = 10;
    private static final double RATIO_MINIMO_CONFIABLE = 0.9;

    private static final int MINIMO_PAQUETES_CERO_DESPERDICIO = 10;
    private static final double RATIO_MINIMO_CERO_DESPERDICIO = 0.8;

    private final PerfilImpactoRepository perfilImpactoRepository;

    public PerfilImpactoService(PerfilImpactoRepository perfilImpactoRepository) {
        this.perfilImpactoRepository = perfilImpactoRepository;
    }

    public PerfilImpacto obtenerOCrear(String propietarioId, TipoPropietario tipoPropietario) {
        return perfilImpactoRepository
                .findByPropietarioIdAndTipoPropietario(propietarioId, tipoPropietario)
                .orElseGet(() -> perfilImpactoRepository.save(new PerfilImpacto(propietarioId, tipoPropietario)));
    }

    public int calcularLimiteReservas(PerfilImpacto perfil) {
        int totalReservas = perfil.getReservasRecogidas() + perfil.getReservasNoRecogidas();
        if (totalReservas == 0) {
            return LIMITE_RESERVAS_BASE;
        }
        double ratio = (double) perfil.getReservasRecogidas() / totalReservas;
        boolean esConfiable = perfil.getReservasRecogidas() >= UMBRAL_RECOGIDAS_GUARDIAN
                && ratio >= RATIO_MINIMO_CONFIABLE;
        return esConfiable ? LIMITE_RESERVAS_CONFIABLE : LIMITE_RESERVAS_BASE;
    }

    public PerfilImpacto registrarRecogida(String clienteId) {
        PerfilImpacto perfil = obtenerOCrear(clienteId, TipoPropietario.CLIENTE);
        perfil.setReservasRecogidas(perfil.getReservasRecogidas() + 1);
        otorgarSiCorresponde(perfil, CodigoInsignia.PRIMER_RESCATE, perfil.getReservasRecogidas() >= 1);
        otorgarSiCorresponde(
                perfil, CodigoInsignia.GUARDIAN_DEL_BARRIO, perfil.getReservasRecogidas() >= UMBRAL_RECOGIDAS_GUARDIAN);
        return perfilImpactoRepository.save(perfil);
    }

    public PerfilImpacto registrarNoRecogida(String clienteId) {
        PerfilImpacto perfil = obtenerOCrear(clienteId, TipoPropietario.CLIENTE);
        perfil.setReservasNoRecogidas(perfil.getReservasNoRecogidas() + 1);
        return perfilImpactoRepository.save(perfil);
    }

    public PerfilImpacto registrarDonacion(String negocioId) {
        PerfilImpacto perfil = obtenerOCrear(negocioId, TipoPropietario.NEGOCIO);
        perfil.setPaquetesDonados(perfil.getPaquetesDonados() + 1);
        evaluarCeroDesperdicio(perfil);
        return perfilImpactoRepository.save(perfil);
    }

    public PerfilImpacto registrarPerdida(String negocioId) {
        PerfilImpacto perfil = obtenerOCrear(negocioId, TipoPropietario.NEGOCIO);
        perfil.setPaquetesPerdidos(perfil.getPaquetesPerdidos() + 1);
        return perfilImpactoRepository.save(perfil);
    }

    private void evaluarCeroDesperdicio(PerfilImpacto perfil) {
        int totalPaquetes = perfil.getPaquetesDonados() + perfil.getPaquetesPerdidos();
        if (totalPaquetes < MINIMO_PAQUETES_CERO_DESPERDICIO) {
            return;
        }
        double ratio = (double) perfil.getPaquetesDonados() / totalPaquetes;
        otorgarSiCorresponde(perfil, CodigoInsignia.CERO_DESPERDICIO, ratio >= RATIO_MINIMO_CERO_DESPERDICIO);
    }

    private void otorgarSiCorresponde(PerfilImpacto perfil, CodigoInsignia codigo, boolean cumpleCondicion) {
        boolean yaLaTiene = perfil.getInsignias().stream().anyMatch(i -> i.getCodigo() == codigo);
        if (cumpleCondicion && !yaLaTiene) {
            perfil.getInsignias().add(new Insignia(codigo, Instant.now()));
        }
    }
}