package cr.ac.una.savora.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Comprobante de un pago SIMULADO. Nunca guarda el número completo de la tarjeta ni el CVV. */
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String marca;

    @Column(name = "ultimos4", nullable = false)
    private String ultimos4;

    @Column(nullable = false)
    private String referencia;

    @Column(nullable = false)
    private OffsetDateTime fecha;

    protected Pago() {
    }

    public Pago(
            Reserva reserva,
            BigDecimal monto,
            String estado,
            String marca,
            String ultimos4,
            String referencia,
            OffsetDateTime fecha) {
        this.reserva = reserva;
        this.monto = monto;
        this.estado = estado;
        this.marca = marca;
        this.ultimos4 = ultimos4;
        this.referencia = referencia;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getEstado() {
        return estado;
    }

    public String getMarca() {
        return marca;
    }

    public String getUltimos4() {
        return ultimos4;
    }

    public String getReferencia() {
        return referencia;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }
}