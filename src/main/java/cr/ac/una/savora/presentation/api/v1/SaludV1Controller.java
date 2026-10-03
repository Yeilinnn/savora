package cr.ac.una.savora.presentation.api.v1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SaludV1Controller {

    @GetMapping("/salud")
    public Map<String, String> salud() {
        return Map.of("estado", "ok", "version", "v1");
    }
}
