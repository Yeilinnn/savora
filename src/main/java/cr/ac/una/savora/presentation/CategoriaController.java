package cr.ac.una.savora.presentation;

import cr.ac.una.savora.business.CategoriaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping("/api/categorias")
    public List<String> listarCategorias() {
        return categoriaService.obtenerCategorias();
    }
}