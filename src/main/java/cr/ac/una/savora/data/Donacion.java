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
@Table(name = "donacion")
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paquete_sorpresa_id", nullable = false, unique = true)
    private PaqueteSorpresa paqueteSorpresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacion_comunitaria_id", nullable = false)
    private OrganizacionComunitaria organizacionComunitaria;

    @Column(name = "fecha_donacion", nullable = false)
    private OffsetDateTime fechaDonacion;

    @Column(nullable = false)
    private String estado;

    protected Donacion() {
    }

    public Donacion(
            PaqueteSorpresa paqueteSorpresa,
            OrganizacionComunitaria organizacionComunitaria,
            OffsetDateTime fechaDonacion,
            String estado) {
        this.paqueteSorpresa = paqueteSorpresa;
        this.organizacionComunitaria = organizacionComunitaria;
        this.fechaDonacion = fechaDonacion;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public PaqueteSorpresa getPaqueteSorpresa() {
        return paqueteSorpresa;
    }

    public void setPaqueteSorpresa(PaqueteSorpresa paqueteSorpresa) {
        this.paqueteSorpresa = paqueteSorpresa;
    }

    public OrganizacionComunitaria getOrganizacionComunitaria() {
        return organizacionComunitaria;
    }

    public void setOrganizacionComunitaria(OrganizacionComunitaria organizacionComunitaria) {
        this.organizacionComunitaria = organizacionComunitaria;
    }

    public OffsetDateTime getFechaDonacion() {
        return fechaDonacion;
    }

    public void setFechaDonacion(OffsetDateTime fechaDonacion) {
        this.fechaDonacion = fechaDonacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
