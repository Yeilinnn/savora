package cr.ac.una.savora.security;

/** Envío de correos de la aplicación. Interfaz para poder sustituirlo en las pruebas. */
public interface EnviadorCorreo {

    void enviar(String para, String asunto, String texto);
}