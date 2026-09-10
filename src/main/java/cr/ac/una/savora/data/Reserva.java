package cr.ac.una.savora.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paquete_sorpresa_id", nullable = false, unique = true)
    private PaqueteSorpresa paqueteSorpresa;

    @Column(name = "fecha_hora_reserva", nullable = false)
    private OffsetDateTime fechaHoraReserva;

    @Column(nullable = false)
    private String estado;

    protected Reserva() {
    }

    public Reserva(Cliente cliente, PaqueteSorpresa paqueteSorpresa, OffsetDateTime fechaHoraReserva, String estado) {
        this.cliente = cliente;
        this.paqueteSorpresa = paqueteSorpresa;
        this.fechaHoraReserva = fechaHoraReserva;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public PaqueteSorpresa getPaqueteSorpresa() {
        return paqueteSorpresa;
    }

    public void setPaqueteSorpresa(PaqueteSorpresa paqueteSorpresa) {
        this.paqueteSorpresa = paqueteSorpresa;
    }

    public OffsetDateTime getFechaHoraReserva() {
        return fechaHoraReserva;
    }

    public void setFechaHoraReserva(OffsetDateTime fechaHoraReserva) {
        this.fechaHoraReserva = fechaHoraReserva;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
