package cr.ac.una.savora.presentation.admin;

import cr.ac.una.savora.business.AdministracionService;
import cr.ac.una.savora.business.dto.UsuarioAdminResumen;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    private final AdministracionService administracionService;

    public UsuarioAdminController(AdministracionService administracionService) {
        this.administracionService = administracionService;
    }

    @GetMapping
    public String listar(Pageable pageable, Model model) {
        Pageable ordenado = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("pagina", administracionService.listarUsuarios(ordenado));
        model.addAttribute("seccionActiva", "usuarios");
        return "admin/usuarios/lista";
    }

    @PostMapping("/{id}/activo")
    public String cambiarActivo(
            @PathVariable Long id,
            @RequestParam boolean valor,
            Authentication autenticacion,
            RedirectAttributes redirectAttributes) {
        UsuarioAdminResumen resumen = administracionService.cambiarActivo(id, valor, autenticacion.getName());
        redirectAttributes.addFlashAttribute(
                "mensaje", "Usuario " + resumen.username() + (resumen.activo() ? " activado." : " desactivado."));
        return "redirect:/admin/usuarios";
    }
}