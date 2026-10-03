package cr.ac.una.savora.business;

import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.ClienteRepository;
import cr.ac.una.savora.business.dto.ClienteResumen;
import cr.ac.una.savora.business.dto.CrearClienteSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteAppService {

    private final ClienteRepository clienteRepository;

    public ClienteAppService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public Page<ClienteResumen> listar(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(ClienteAppService::aResponse);
    }

    @Transactional(readOnly = true)
    public ClienteResumen obtenerPorId(Long id) {
        return aResponse(buscar(id));
    }

    @Transactional
    public ClienteResumen crear(CrearClienteSolicitud request) {
        Cliente cliente = clienteRepository.save(
                new Cliente(request.nombre(), request.correo(), request.telefono()));
        return aResponse(cliente);
    }

    private Cliente buscar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
    }

    private static ClienteResumen aResponse(Cliente cliente) {
        return new ClienteResumen(cliente.getId(), cliente.getNombre(), cliente.getCorreo(), cliente.getTelefono());
    }
}
