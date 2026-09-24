package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CerrarPaqueteRequest;
import cr.ac.una.savora.business.dto.CierreResumen;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.business.excepcion.TransicionEstadoInvalidaException;
import cr.ac.una.savora.data.Categoria;
import cr.ac.una.savora.data.Donacion;
import cr.ac.una.savora.data.DonacionRepository;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.OrganizacionComunitaria;
import cr.ac.una.savora.data.OrganizacionComunitariaRepository;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.PaqueteSorpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CierreDePaquetesServiceTest {

    @Mock
    PaqueteSorpresaRepository paqueteSorpresaRepository;
    @Mock
    OrganizacionComunitariaRepository organizacionComunitariaRepository;
    @Mock
    DonacionRepository donacionRepository;

    CierreDePaquetesService service;

    final Clock clock = Clock.fixed(Instant.parse("2026-08-27T16:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new CierreDePaquetesService(
                paqueteSorpresaRepository, organizacionComunitariaRepository, donacionRepository, clock);
    }

    private PaqueteSorpresa paquete(Long id, String estado, int cantidad) {
        Negocio negocio = new Negocio("Soda Doña Marta", "Soda", "Nicoya", LocalTime.of(20, 0));
        ReflectionTestUtils.setField(negocio, "id", 2L);
        Categoria categoria = new Categoria("Comida preparada");
        ReflectionTestUtils.setField(categoria, "id", 2L);
        PaqueteSorpresa paquete = new PaqueteSorpresa(
                negocio, categoria, "Sopa y arroz sobrante", cantidad,
                new BigDecimal("3500.00"), new BigDecimal("1500.00"),
                OffsetDateTime.parse("2026-08-19T20:00:00-06:00"), estado);
        ReflectionTestUtils.setField(paquete, "id", id);
        return paquete;
    }

    private OrganizacionComunitaria organizacion(Long id, String nombre, int capacidad) {
        OrganizacionComunitaria org = new OrganizacionComunitaria(nombre, "ONG", capacidad, "2666-0000");
        ReflectionTestUtils.setField(org, "id", id);
        return org;
    }

    @Test
    void donaElPaqueteALaOrganizacionConMenorCapacidadQueAunAsiAlcanza() {
        PaqueteSorpresa paquete = paquete(4L, "disponible", 4);
        OrganizacionComunitaria chica = organizacion(1L, "Comedor Esperanza", 20);
        OrganizacionComunitaria grande = organizacion(2L, "Banco de Alimentos", 50);

        when(paqueteSorpresaRepository.findById(4L)).thenReturn(Optional.of(paquete));
        when(organizacionComunitariaRepository.findAll()).thenReturn(List.of(grande, chica));
        when(paqueteSorpresaRepository.save(any(PaqueteSorpresa.class))).thenAnswer(inv -> inv.getArgument(0));
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(inv -> inv.getArgument(0));

        CierreResumen resumen = service.cerrar(new CerrarPaqueteRequest(4L));

        assertThat(resumen.resultado()).isEqualTo("DONADO");
        assertThat(resumen.organizacionComunitariaId()).isEqualTo(1L); // la chica, no la grande
        assertThat(paquete.getEstado()).isEqualTo("donado");
        verify(donacionRepository).save(any(Donacion.class));
    }

    @Test
    void marcaComoPerdidoSiNingunaOrganizacionAlcanza() {
        PaqueteSorpresa paquete = paquete(4L, "disponible", 100);
        OrganizacionComunitaria chica = organizacion(1L, "Comedor Esperanza", 20);

        when(paqueteSorpresaRepository.findById(4L)).thenReturn(Optional.of(paquete));
        when(organizacionComunitariaRepository.findAll()).thenReturn(List.of(chica));
        when(paqueteSorpresaRepository.save(any(PaqueteSorpresa.class))).thenAnswer(inv -> inv.getArgument(0));

        CierreResumen resumen = service.cerrar(new CerrarPaqueteRequest(4L));

        assertThat(resumen.resultado()).isEqualTo("PERDIDO");
        assertThat(paquete.getEstado()).isEqualTo("perdido");
        verify(donacionRepository, never()).save(any());
    }

    @Test
    void lanzaExcepcionSiElPaqueteNoExiste() {
        when(paqueteSorpresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cerrar(new CerrarPaqueteRequest(99L)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void lanzaExcepcionSiElPaqueteYaFueReservadoJustoAntesDelCierre() {
        PaqueteSorpresa paquete = paquete(4L, "reservado", 4);
        OrganizacionComunitaria org = organizacion(1L, "Comedor Esperanza", 20);

        when(paqueteSorpresaRepository.findById(4L)).thenReturn(Optional.of(paquete));
        when(organizacionComunitariaRepository.findAll()).thenReturn(List.of(org));

        assertThatThrownBy(() -> service.cerrar(new CerrarPaqueteRequest(4L)))
                .isInstanceOf(TransicionEstadoInvalidaException.class);

        verify(donacionRepository, never()).save(any());
    }

    @Test
    void siNingunaOrganizacionAlcanzaYElPaqueteYaNoEstaDisponibleTambienLanzaExcepcion() {
        PaqueteSorpresa paquete = paquete(4L, "recogido", 100);

        when(paqueteSorpresaRepository.findById(4L)).thenReturn(Optional.of(paquete));
        when(organizacionComunitariaRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> service.cerrar(new CerrarPaqueteRequest(4L)))
                .isInstanceOf(TransicionEstadoInvalidaException.class);
    }
}