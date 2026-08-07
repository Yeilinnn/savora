package cr.ac.una.savora.business;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    public List<String> obtenerCategorias() {
        return List.of("Panadería", "Comida preparada", "Frutas y verduras");
    }
}