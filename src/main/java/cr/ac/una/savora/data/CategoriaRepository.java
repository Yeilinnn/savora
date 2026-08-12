package cr.ac.una.savora.data;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoriaRepository {

    public List<String> findAll() {
        return List.of("Panadería", "Comida preparada", "Frutas y verduras");
    }
}
