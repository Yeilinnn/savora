package cr.ac.una.savora.business.dto;

public record NegocioAdminResumen(
        Long id, String nombre, String tipoNegocio, String ubicacion, String estado, String usuario) {
}