package cr.ac.una.savora.presentation.admin.formulario;

import jakarta.validation.constraints.NotBlank;

public class CategoriaFormulario {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
