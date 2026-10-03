package cr.ac.una.savora.business.dto;

import java.util.List;

public record PerfilImpactoResumen(
        String propietarioId,
        String tipoPropietario,
        double kilogramosRescatados,
        int reservasRecogidas,
        int reservasNoRecogidas,
        int paquetesDonados,
        int paquetesPerdidos,
        List<String> insignias,
        int limiteReservasActivas) {
}
