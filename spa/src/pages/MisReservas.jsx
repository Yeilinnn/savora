import { api } from '../api/client.js'
import EstadoCarga from '../components/EstadoCarga.jsx'
import Paginacion from '../components/Paginacion.jsx'
import usePagina from '../components/usePagina.js'
import { visualDe } from '../components/categorias.js'
import { ahorroDeReserva, formatearFechaHora, formatearMoneda } from '../components/Formato.js'

const TAMANO = 8

function etiquetaEstado(estado) {
  const texto = (estado || '').replace(/_/g, ' ')
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

function TarjetaReserva({ reserva }) {
  const visual = visualDe(reserva.descripcionPaquete)
  const estado = (reserva.estado || '').toLowerCase()
  const ahorro = ahorroDeReserva(reserva)
  return (
    <li className="reserva">
      <div className={`reserva-imagen ${visual.clase}`} aria-hidden="true">
        {visual.emoji}
      </div>
      <div className="reserva-datos">
        <p className="etiqueta-seccion">{formatearFechaHora(reserva.fechaHoraReserva)}</p>
        <h3>{reserva.descripcionPaquete}</h3>
        {ahorro > 0 && <p className="negocio">Ahorraste {formatearMoneda(ahorro)}</p>}
      </div>
      <div className="reserva-lado">
        <span className="reserva-precio">{formatearMoneda(reserva.precioConDescuento)}</span>
        <span className={`insignia-estado estado-${estado}`}>{etiquetaEstado(reserva.estado)}</span>
      </div>
    </li>
  )
}

export default function MisReservas() {
  const { estado, pagina, error, irAPagina, reintentar } = usePagina(api.listarReservas, TAMANO)

  const activas = (pagina?.contenido ?? []).filter((r) => (r.estado || '').toLowerCase() === 'pendiente')
  const historial = (pagina?.contenido ?? []).filter((r) => (r.estado || '').toLowerCase() !== 'pendiente')

  return (
    <section>
      <p className="etiqueta-seccion naranja">Tu historial</p>
      <h1>Mis reservas</h1>
      <p className="sub">Guarda tus paquetes y revisa cuándo pasar por ellos.</p>

      <EstadoCarga estado={estado} error={error} alReintentar={reintentar}>
        {pagina && pagina.contenido.length === 0 ? (
          <p className="estado">Todavía no tienes reservas. ¡Explora los paquetes y rescata el primero!</p>
        ) : (
          pagina && (
            <>
              {activas.length > 0 && (
                <>
                  <h2 className="titulo-lista">Por recoger</h2>
                  <ul className="lista-reservas">
                    {activas.map((r) => (
                      <TarjetaReserva key={r.id} reserva={r} />
                    ))}
                  </ul>
                </>
              )}
              {historial.length > 0 && (
                <>
                  <h2 className="titulo-lista">Historial</h2>
                  <ul className="lista-reservas">
                    {historial.map((r) => (
                      <TarjetaReserva key={r.id} reserva={r} />
                    ))}
                  </ul>
                </>
              )}
              <Paginacion pagina={pagina} alCambiar={irAPagina} />
            </>
          )
        )}
      </EstadoCarga>
    </section>
  )
}