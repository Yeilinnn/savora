package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos de la tarjeta de prueba. No se guardan; solo se usan para simular la autorización. */
public record TarjetaSimulada(
        @NotBlank
        @Pattern(regexp = "^[0-9 ]{13,23}$", message = "El número de tarjeta debe tener entre 13 y 19 dígitos")
        String numero,
        @NotBlank @Size(max = 60) String titular,
        @NotBlank
        @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "El vencimiento debe tener el formato MM/AA")
        String vencimiento,
        @NotBlank @Pattern(regexp = "^[0-9]{3,4}$", message = "El CVV debe tener 3 o 4 dígitos") String cvv) {

    /** Evita que la tarjeta aparezca en los logs si alguien imprime el objeto. */
    @Override
    public String toString() {
        return "TarjetaSimulada[protegida]";
    }
}