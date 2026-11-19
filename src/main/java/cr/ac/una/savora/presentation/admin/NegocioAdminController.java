package cr.ac.una.savora.presentation.admin;

import cr.ac.una.savora.business.AdministracionService;
import cr.ac.una.savora.business.dto.NegocioAdminResumen;
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
@RequestMapping("/admin/negocios")
public class NegocioAdminController {

    private final AdministracionService administracionService;

    public NegocioAdminController(AdministracionService administracionService) {
        this.administracionService = administracionService;
    }

    @GetMapping
    public String listar(Pageable pageable, Model model) {
        Pageable ordenado = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("pagina", administracionService.listarNegocios(ordenado));
        model.addAttribute("pendientes", administracionService.contarNegociosPendientes());
        model.addAttribute("seccionActiva", "negocios");
        return "admin/negocios/lista";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            Authentication autenticacion,
            RedirectAttributes redirectAttributes) {
        NegocioAdminResumen resumen = administracionService.cambiarEstadoNegocio(id, estado, autenticacion.getName());
        redirectAttributes.addFlashAttribute(
                "mensaje", "Negocio \"" + resumen.nombre() + "\" ahora está " + resumen.estado() + ".");
        return "redirect:/admin/negocios";
    }
}