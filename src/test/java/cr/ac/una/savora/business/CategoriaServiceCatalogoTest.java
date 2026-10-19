package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CrearCategoriaSolicitud;
import cr.ac.una.savora.data.Categoria;
import cr.ac.una.savora.data.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceCatalogoTest {

    @Mock
    CategoriaRepository categoriaRepository;

    @InjectMocks
    CategoriaService service;

    @Test
    void crearDevuelveResumen() {
        when(categoriaRepository.save(org.mockito.ArgumentMatchers.any(Categoria.class))).thenAnswer(inv -> {
            Categoria c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 9L);
            return c;
        });

        assertThat(service.crear(new CrearCategoriaSolicitud("Bebidas")).nombre()).isEqualTo("Bebidas");
    }

    @Test
    void actualizarCambiaNombre() {
        Categoria categoria = new Categoria("Panadería");
        ReflectionTestUtils.setField(categoria, "id", 3L);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));

        assertThat(service.actualizar(3L, new CrearCategoriaSolicitud("Repostería")).nombre())
                .isEqualTo("Repostería");
    }

    @Test
    void eliminarBorraCategoria() {
        Categoria categoria = new Categoria("Panadería");
        ReflectionTestUtils.setField(categoria, "id", 3L);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));

        service.eliminar(3L);

        verify(categoriaRepository).delete(categoria);
    }
}
