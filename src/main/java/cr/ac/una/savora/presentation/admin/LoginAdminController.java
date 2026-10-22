package cr.ac.una.savora.presentation.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginAdminController {

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String denegado,
            Model model) {
        if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada correctamente.");
        }
        if (denegado != null) {
            model.addAttribute("error", "No tiene permiso para acceder al módulo administrativo.");
        }
        return "login";
    }
}
