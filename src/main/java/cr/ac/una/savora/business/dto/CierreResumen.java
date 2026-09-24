package cr.ac.una.savora.business.dto;

public record CierreResumen(
        Long paqueteSorpresaId,
        String resultado,
        Long organizacionComunitariaId,
        String nombreOrganizacion) {
}