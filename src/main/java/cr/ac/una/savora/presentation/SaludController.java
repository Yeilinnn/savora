package cr.ac.una.savora.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SaludController {

    @GetMapping("/api/salud")
    public Map<String, String> verificarSalud() {
        return Map.of("status", "UP", "sistema", "Savora");
    }
}
