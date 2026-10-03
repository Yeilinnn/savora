package cr.ac.una.savora.business.dto;

import java.util.List;

public record CierreResumen(
        Long paqueteSorpresaId,
        String resultado,
        Long organizacionComunitariaId,
        String nombreOrganizacion,
        int kilogramosDonados,
        int kilogramosAcumuladosNegocio,
        List<Long> organizacionesRechazadas) {
}