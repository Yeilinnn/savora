package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.PaqueteSorpresaResumen;
import cr.ac.una.savora.business.dto.ResenaPromedio;
import cr.ac.una.savora.data.Categoria;
import cr.ac.una.savora.data.CategoriaRepository;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.NegocioRepository;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.PaqueteSorpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaqueteSorpresaPosicionamientoTest {

    @Mock
    PaqueteSorpresaRepository paqueteSorpresaRepository;
    @Mock
    NegocioRepository negocioRepository;
    @Mock
    CategoriaRepository categoriaRepository;
    @Mock
    PerfilImpactoService perfilImpactoService;
    @Mock
    ResenaService resenaService;

    PaqueteSorpresaAppService service;

    @BeforeEach
    void setUp() {
        service = new PaqueteSorpresaAppService(
                paqueteSorpresaRepository, negocioRepository, categoriaRepository, perfilImpactoService, resenaService);
    }

    private PaqueteSorpresa paquete(long id, long negocioId, String hora) {
        Negocio negocio = new Negocio("Negocio " + negocioId, "Tipo", "Liberia", LocalTime.of(19, 0));
        ReflectionTestUtils.setField(negocio, "id", negocioId);
        Categoria categoria = new Categoria("Panadería");
        ReflectionTestUtils.setField(categoria, "id", 1L);
        PaqueteSorpresa p = new PaqueteSorpresa(
                negocio, categoria, "Paquete " + id, 1,
                BigDecimal.valueOf(5000), BigDecimal.valueOf(2500), OffsetDateTime.parse(hora), "disponible");
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    @SuppressWarnings("unchecked")
    private void catalogo() {
        // Orden pedido (hora límite): 1 (negocio 10), 2 (negocio 20), 3 (negocio 30), 4 (negocio 20)
        when(paqueteSorpresaRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(
                paquete(1, 10, "2026-11-19T20:00:00Z"),
                paquete(2, 20, "2026-11-19T21:00:00Z"),
                paquete(3, 30, "2026-11-19T22:00:00Z"),
                paquete(4, 20, "2026-11-19T23:00:00Z")));
        when(perfilImpactoService.negociosDestacados(any())).thenReturn(Set.of(30L));
        when(resenaService.promediosPorNegocio(any())).thenReturn(Map.of(
                10L, new ResenaPromedio(10L, 3.0, 2),
                20L, new ResenaPromedio(20L, 4.8, 5)));
    }

    @Test
    void losNegociosDestacadosSalenPrimeroYLuegoLosMejorCalificados() {
        catalogo();

        Page<PaqueteSorpresaResumen> pagina =
                service.listar("disponible", null, null, true, PageRequest.of(0, 10));

        assertThat(pagina.getContent()).extracting(PaqueteSorpresaResumen::id).containsExactly(3L, 2L, 4L, 1L);
        assertThat(pagina.getContent().getFirst().negocioDestacado()).isTrue();
        assertThat(pagina.getContent().get(1).promedioResenas()).isEqualTo(4.8);
        assertThat(pagina.getContent().get(1).totalResenas()).isEqualTo(5);
        assertThat(pagina.getTotalElements()).isEqualTo(4);
    }

    @Test
    void elPosicionamientoSeAplicaAntesDePaginar() {
        catalogo();

        Page<PaqueteSorpresaResumen> segunda =
                service.listar("disponible", null, null, true, PageRequest.of(1, 2));

        assertThat(segunda.getContent()).extracting(PaqueteSorpresaResumen::id).containsExactly(4L, 1L);
        assertThat(segunda.getTotalElements()).isEqualTo(4);
    }

    @Test
    void unaPaginaFueraDeRangoVieneVacia() {
        catalogo();

        Page<PaqueteSorpresaResumen> pagina =
                service.listar("disponible", null, null, true, PageRequest.of(5, 2));

        assertThat(pagina.getContent()).isEmpty();
    }
}