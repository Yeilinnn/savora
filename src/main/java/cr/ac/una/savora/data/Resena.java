package cr.ac.una.savora.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Reseña de un paquete recogido. Vive en MongoDB (colección "resena"): se lee siempre junto al
 * negocio que se está mostrando y crece sin límite. Una reseña por reserva (índice único).
 */
@Document(collection = "resena")
public class Resena {

    @Id
    private String id;

    @Indexed(unique = true)
    private Long reservaId;

    @Indexed
    private Long negocioId;

    private Long paqueteSorpresaId;
    private Long clienteId;
    private String nombreCliente;
    private int calificacion;
    private String comentario;
    private Instant fecha;

    public Resena() {
    }

    public Resena(
            Long reservaId,
            Long negocioId,
            Long paqueteSorpresaId,
            Long clienteId,
            String nombreCliente,
            int calificacion,
            String comentario,
            Instant fecha) {
        this.reservaId = reservaId;
        this.negocioId = negocioId;
        this.paqueteSorpresaId = paqueteSorpresaId;
        this.clienteId = clienteId;
        this.nombreCliente = nombreCliente;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    public String getId() {
        return id;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public Long getNegocioId() {
        return negocioId;
    }

    public Long getPaqueteSorpresaId() {
        return paqueteSorpresaId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public Instant getFecha() {
        return fecha;
    }
}