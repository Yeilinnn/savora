package cr.ac.una.savora.business.dto;

public record LoginRespuesta(
        String token,
        String rol,
        long expiraEnSegundos) {
}