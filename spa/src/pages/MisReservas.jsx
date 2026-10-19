import { api } from '../api/client.js'
import EstadoCarga from '../components/EstadoCarga.jsx'
import Paginacion from '../components/Paginacion.jsx'
import usePagina from '../components/usePagina.js'
import { formatearFechaHora, formatearMoneda } from '../components/Formato.js'

const TAMANO = 8

export default function MisReservas() {
  const { estado, pagina, error, irAPagina, reintentar } = usePagina(api.listarReservas, TAMANO)

  return (
    <section>
      <h1>Mis reservas</h1>
      <p className="sub">Historial de sus reservas, de la más reciente a la más antigua.</p>

      <EstadoCarga estado={estado} error={error} alReintentar={reintentar}>
        {pagina && pagina.contenido.length === 0 ? (
          <p className="estado">Todavía no tiene reservas.</p>
        ) : (
          pagina && (
            <>
              <table>
                <thead>
                  <tr>
                    <th>Paquete</th>
                    <th>Fecha</th>
                    <th className="num">Precio</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {pagina.contenido.map((r) => (
                    <tr key={r.id}>
                      <td>{r.descripcionPaquete}</td>
                      <td>{formatearFechaHora(r.fechaHoraReserva)}</td>
                      <td className="num">{formatearMoneda(r.precioConDescuento)}</td>
                      <td>
                        <span className="etiqueta">{r.estado}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <Paginacion pagina={pagina} alCambiar={irAPagina} />
            </>
          )
        )}
      </EstadoCarga>
    </section>
  )
}
