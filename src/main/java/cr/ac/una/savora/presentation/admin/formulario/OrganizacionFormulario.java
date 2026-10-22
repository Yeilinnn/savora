package cr.ac.una.savora.presentation.admin.formulario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class OrganizacionFormulario {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @Min(value = 1, message = "La capacidad debe ser al menos 1 kg")
    private int capacidadRecoleccion;

    @NotBlank(message = "El contacto es obligatorio")
    private String contacto;

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
