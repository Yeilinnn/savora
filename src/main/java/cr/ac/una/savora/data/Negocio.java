package cr.ac.una.savora.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalTime;

@Entity
@Table(name = "negocio")
public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "tipo_negocio", nullable = false)
    private String tipoNegocio;

    @Column(nullable = false)
    private String ubicacion;

    @Column(name = "horario_cierre", nullable = false)
    private LocalTime horarioCierre;

    protected Negocio() {
    }

    public Negocio(String nombre, String tipoNegocio, String ubicacion, LocalTime horarioCierre) {
        this.nombre = nombre;
        this.tipoNegocio = tipoNegocio;
        this.ubicacion = ubicacion;
        this.horarioCierre = horarioCierre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }

    public void setTipoNegocio(String tipoNegocio) {
        this.tipoNegocio = tipoNegocio;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalTime getHorarioCierre() {
        return horarioCierre;
    }

    public void setHorarioCierre(LocalTime horarioCierre) {
        this.horarioCierre = horarioCierre;
    }
}