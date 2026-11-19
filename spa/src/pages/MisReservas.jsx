import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client.js'
import EstadoCarga from '../components/EstadoCarga.jsx'
import FormularioResena from '../components/FormularioResena.jsx'
import Paginacion from '../components/Paginacion.jsx'
import usePagina from '../components/usePagina.js'
import { visualDe } from '../components/categorias.js'
import { ahorroDeReserva, formatearFechaHora, formatearMoneda } from '../components/Formato.js'

const TAMANO = 8

function etiquetaEstado(estado) {
  const texto = (estado || '').replace(/_/g, ' ')
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

function TarjetaReserva({ reserva, calificada, abierta, alAbrir, alCerrar, alCalificar }) {
  const visual = visualDe(reserva.descripcionPaquete)
  const estado = (reserva.estado || '').toLowerCase()
  const ahorro = ahorroDeReserva(reserva)
  const puedeCalificar = estado === 'recogido' && !calificada
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
        {puedeCalificar && !abierta && (
          <button type="button" onClick={() => alAbrir(reserva.id)}>
            Calificar
          </button>
        )}
        {estado === 'recogido' && calificada && <span className="negocio">★ Ya calificaste</span>}
      </div>
      {puedeCalificar && abierta && (
        <div className="reserva-resena">
          <FormularioResena reservaId={reserva.id} alEnviar={alCalificar} alCancelar={alCerrar} />
        </div>
      )}
    </li>
  )
}

export default function MisReservas() {
  const { estado, pagina, error, irAPagina, reintentar } = usePagina(api.listarReservas, TAMANO)
  const [calificadas, setCalificadas] = useState(() => new Set())
  const [abierta, setAbierta] = useState(null)

  // Qué reservas ya tienen reseña. Si falla, simplemente no se oculta el botón (el servidor igual evita duplicados).
  useEffect(() => {
    api
      .reservasCalificadas()
      .then((ids) => setCalificadas(new Set(ids)))
      .catch(() => {})
  }, [])

  const alCalificar = useCallback((reservaId) => {
    setCalificadas((previas) => new Set(previas).add(reservaId))
    setAbierta(null)
  }, [])

  const activas = (pagina?.contenido ?? []).filter((r) => (r.estado || '').toLowerCase() === 'pendiente')
  const historial = (pagina?.contenido ?? []).filter((r) => (r.estado || '').toLowerCase() !== 'pendiente')

  const tarjeta = (r) => (
    <TarjetaReserva
      key={r.id}
      reserva={r}
      calificada={calificadas.has(r.id)}
      abierta={abierta === r.id}
      alAbrir={setAbierta}
      alCerrar={() => setAbierta(null)}
      alCalificar={alCalificar}
    />
  )

  return (
    <section>
      <p className="etiqueta-seccion naranja">Tu historial</p>
      <h1>Mis reservas</h1>
      <p className="sub">Guarda tus paquetes y revisa cuándo pasar por ellos. Cuando recojas uno, cuéntanos cómo te fue.</p>

      <EstadoCarga estado={estado} error={error} alReintentar={reintentar}>
        {pagina && pagina.contenido.length === 0 ? (
          <p className="estado">Todavía no tienes reservas. ¡Explora los paquetes y rescata el primero!</p>
        ) : (
          pagina && (
            <>
              {activas.length > 0 && (
                <>
                  <h2 className="titulo-lista">Por recoger</h2>
                  <ul className="lista-reservas">{activas.map(tarjeta)}</ul>
                </>
              )}
              {historial.length > 0 && (
                <>
                  <h2 className="titulo-lista">Historial</h2>
                  <ul className="lista-reservas">{historial.map(tarjeta)}</ul>
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