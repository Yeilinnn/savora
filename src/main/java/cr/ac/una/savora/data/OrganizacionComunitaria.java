package cr.ac.una.savora.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizacion_comunitaria")
public class OrganizacionComunitaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipo;

    @Column(name = "capacidad_recoleccion", nullable = false)
    private int capacidadRecoleccion;

    @Column(nullable = false)
    private String contacto;

    protected OrganizacionComunitaria() {
    }

    public OrganizacionComunitaria(String nombre, String tipo, int capacidadRecoleccion, String contacto) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidadRecoleccion = capacidadRecoleccion;
        this.contacto = contacto;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCapacidadRecoleccion() {
        return capacidadRecoleccion;
    }

    public void setCapacidadRecoleccion(int capacidadRecoleccion) {
        this.capacidadRecoleccion = capacidadRecoleccion;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }
}