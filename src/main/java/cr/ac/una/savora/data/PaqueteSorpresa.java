package cr.ac.una.savora.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "paquete_sorpresa")
public class PaqueteSorpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_original", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioOriginal;

    @Column(name = "precio_con_descuento", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioConDescuento;

    @Column(name = "hora_limite_recogida", nullable = false)
    private Instant horaLimiteRecogida;

    @Column(nullable = false)
    private String estado;

    protected PaqueteSorpresa() {
    }

    public PaqueteSorpresa(
            Negocio negocio,
            Categoria categoria,
            String descripcion,
            int cantidad,
            BigDecimal precioOriginal,
            BigDecimal precioConDescuento,
            Instant horaLimiteRecogida,
            String estado) {
        this.negocio = negocio;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioOriginal = precioOriginal;
        this.precioConDescuento = precioConDescuento;
        this.horaLimiteRecogida = horaLimiteRecogida;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public Negocio getNegocio() {
        return negocio;
    }

    public void setNegocio(Negocio negocio) {
        this.negocio = negocio;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioOriginal() {
        return precioOriginal;
    }

    public void setPrecioOriginal(BigDecimal precioOriginal) {
        this.precioOriginal = precioOriginal;
    }

    public BigDecimal getPrecioConDescuento() {
        return precioConDescuento;
    }

    public void setPrecioConDescuento(BigDecimal precioConDescuento) {
        this.precioConDescuento = precioConDescuento;
    }

    public Instant getHoraLimiteRecogida() {
        return horaLimiteRecogida;
    }

    public void setHoraLimiteRecogida(Instant horaLimiteRecogida) {
        this.horaLimiteRecogida = horaLimiteRecogida;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}