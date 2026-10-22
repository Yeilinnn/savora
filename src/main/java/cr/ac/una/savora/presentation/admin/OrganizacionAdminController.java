package cr.ac.una.savora.presentation.admin;

import cr.ac.una.savora.business.OrganizacionAppService;
import cr.ac.una.savora.business.dto.CrearOrganizacionSolicitud;
import cr.ac.una.savora.business.dto.OrganizacionResumen;
import cr.ac.una.savora.presentation.admin.formulario.OrganizacionFormulario;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/organizaciones")
public class OrganizacionAdminController {

    private final OrganizacionAppService organizacionAppService;

    public OrganizacionAdminController(OrganizacionAppService organizacionAppService) {
        this.organizacionAppService = organizacionAppService;
    }

    @GetMapping
    public String listar(Pageable pageable, Model model) {
        Page<OrganizacionResumen> pagina = organizacionAppService.listar(pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("seccionActiva", "organizaciones");
        return "admin/organizaciones/lista";
    }

    @GetMapping("/nueva")
    public String formularioNueva(Model model) {
        model.addAttribute("organizacionFormulario", new OrganizacionFormulario());
        model.addAttribute("seccionActiva", "organizaciones");
        model.addAttribute("tituloFormulario", "Nueva organización");
        return "admin/organizaciones/formulario";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("organizacionFormulario") OrganizacionFormulario formulario,
            BindingResult errores,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (errores.hasErrors()) {
            prepararFormularioOrganizacion(model, "Nueva organización", null);
            return "admin/organizaciones/formulario";
        }
        OrganizacionResumen creada = organizacionAppService.crear(aSolicitud(formulario));
        redirectAttributes.addFlashAttribute("mensaje", "Organización \"" + creada.nombre() + "\" creada.");
        return "redirect:/admin/organizaciones";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        OrganizacionResumen org = organizacionAppService.obtenerPorId(id);
        OrganizacionFormulario formulario = new OrganizacionFormulario();
        formulario.setNombre(org.nombre());
        formulario.setTipo(org.tipo());
        formulario.setCapacidadRecoleccion(org.capacidadRecoleccion());
        formulario.setContacto(org.contacto());
        model.addAttribute("organizacionFormulario", formulario);
        model.addAttribute("organizacionId", id);
        model.addAttribute("seccionActiva", "organizaciones");
        model.addAttribute("tituloFormulario", "Editar organización");
        return "admin/organizaciones/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("organizacionFormulario") OrganizacionFormulario formulario,
            BindingResult errores,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (errores.hasErrors()) {
            prepararFormularioOrganizacion(model, "Editar organización", id);
            return "admin/organizaciones/formulario";
        }
        organizacionAppService.actualizar(id, aSolicitud(formulario));
        redirectAttributes.addFlashAttribute("mensaje", "Organización actualizada.");
        return "redirect:/admin/organizaciones";
    }

    private void prepararFormularioOrganizacion(Model model, String titulo, Long id) {
        model.addAttribute("seccionActiva", "organizaciones");
        model.addAttribute("tituloFormulario", titulo);
        if (id != null) {
            model.addAttribute("organizacionId", id);
        }
    }

    private static CrearOrganizacionSolicitud aSolicitud(OrganizacionFormulario formulario) {
        return new CrearOrganizacionSolicitud(
                formulario.getNombre(),
                formulario.getTipo(),
                formulario.getCapacidadRecoleccion(),
                formulario.getContacto());
    }
}
