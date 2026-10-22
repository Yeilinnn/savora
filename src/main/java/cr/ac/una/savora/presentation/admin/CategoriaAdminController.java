package cr.ac.una.savora.presentation.admin;

import cr.ac.una.savora.business.CategoriaService;
import cr.ac.una.savora.business.dto.CategoriaResumen;
import cr.ac.una.savora.business.dto.CrearCategoriaSolicitud;
import cr.ac.una.savora.presentation.admin.formulario.CategoriaFormulario;
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
@RequestMapping("/admin/categorias")
public class CategoriaAdminController {

    private final CategoriaService categoriaService;

    public CategoriaAdminController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(Pageable pageable, Model model) {
        Page<CategoriaResumen> pagina = categoriaService.listar(pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("seccionActiva", "categorias");
        return "admin/categorias/lista";
    }

    @GetMapping("/nueva")
    public String formularioNueva(Model model) {
        model.addAttribute("categoriaFormulario", new CategoriaFormulario());
        model.addAttribute("seccionActiva", "categorias");
        model.addAttribute("tituloFormulario", "Nueva categoría");
        return "admin/categorias/formulario";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("categoriaFormulario") CategoriaFormulario formulario,
            BindingResult errores,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (errores.hasErrors()) {
            prepararFormularioCategoria(model, "Nueva categoría", null);
            return "admin/categorias/formulario";
        }
        CategoriaResumen creada = categoriaService.crear(new CrearCategoriaSolicitud(formulario.getNombre()));
        redirectAttributes.addFlashAttribute("mensaje", "Categoría \"" + creada.nombre() + "\" creada.");
        return "redirect:/admin/categorias";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        CategoriaResumen categoria = categoriaService.obtenerPorId(id);
        CategoriaFormulario formulario = new CategoriaFormulario();
        formulario.setNombre(categoria.nombre());
        model.addAttribute("categoriaFormulario", formulario);
        model.addAttribute("categoriaId", id);
        model.addAttribute("seccionActiva", "categorias");
        model.addAttribute("tituloFormulario", "Editar categoría");
        return "admin/categorias/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("categoriaFormulario") CategoriaFormulario formulario,
            BindingResult errores,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (errores.hasErrors()) {
            prepararFormularioCategoria(model, "Editar categoría", id);
            return "admin/categorias/formulario";
        }
        categoriaService.actualizar(id, new CrearCategoriaSolicitud(formulario.getNombre()));
        redirectAttributes.addFlashAttribute("mensaje", "Categoría actualizada.");
        return "redirect:/admin/categorias";
    }

    private void prepararFormularioCategoria(Model model, String titulo, Long id) {
        model.addAttribute("seccionActiva", "categorias");
        model.addAttribute("tituloFormulario", titulo);
        if (id != null) {
            model.addAttribute("categoriaId", id);
        }
    }
}
