package cr.ac.una.savora.business.dto;

public record OrganizacionResumen(
        Long id,
        String nombre,
        String tipo,
        int capacidadRecoleccion,
        String contacto) {
}
