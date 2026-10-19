import { useState } from 'react'
import { api } from '../api/client.js'
import EstadoCarga from '../components/EstadoCarga.jsx'
import Paginacion from '../components/Paginacion.jsx'
import usePagina from '../components/usePagina.js'
import { formatearFechaHora, formatearMoneda } from '../components/Formato.js'

const TAMANO = 6

function mensajeDeError(e) {
  if (e.estado === 422) return `No se pudo reservar: ${e.message}`
  if (e.estado === 404) return 'El paquete ya no existe.'
  if (e.estado === 403) return 'Su cuenta no puede realizar reservas.'
  return e.message
}

export default function Paquetes() {
  const { estado, pagina, error, irAPagina, reintentar } = usePagina(api.listarPaquetes, TAMANO)
  const [reservando, setReservando] = useState(null)
  const [aviso, setAviso] = useState(null)

  async function reservar(paquete) {
    setAviso(null)
    setReservando(paquete.id)
    try {
      await api.reservar(paquete.id)
      setAviso({ tipo: 'ok', texto: `Reserva creada: ${paquete.descripcion}` })
      reintentar()
    } catch (e) {
      setAviso({ tipo: 'error', texto: mensajeDeError(e) })
    } finally {
      setReservando(null)
    }
  }

  return (
    <section>
      <h1>Paquetes disponibles</h1>
      <p className="sub">Reserve un paquete sorpresa y rescate comida del día.</p>

      {aviso && (
        <p className={`alerta ${aviso.tipo === 'error' ? 'error' : 'ok'}`} role="status">
          {aviso.texto}
        </p>
      )}

      <EstadoCarga estado={estado} error={error} alReintentar={reintentar}>
        {pagina && pagina.contenido.length === 0 ? (
          <p className="estado">No hay paquetes disponibles por ahora.</p>
        ) : (
          pagina && (
            <>
              <ul className="rejilla">
                {pagina.contenido.map((p) => (
                  <li key={p.id} className="paquete">
                    <h2>{p.descripcion}</h2>
                    <p className="meta">
                      {p.nombreNegocio} · {p.nombreCategoria}
                    </p>
                    <p>
                      <span className="precio">{formatearMoneda(p.precioConDescuento)}</span>{' '}
                      <s className="tachado">{formatearMoneda(p.precioOriginal)}</s>
                    </p>
                    <p className="meta">
                      Quedan {p.cantidad} · Recoger antes de {formatearFechaHora(p.horaLimiteRecogida)}
                    </p>
                    <button
                      type="button"
                      className="primario"
                      onClick={() => reservar(p)}
                      disabled={reservando !== null}
                    >
                      {reservando === p.id ? 'Reservando…' : 'Reservar'}
                    </button>
                  </li>
                ))}
              </ul>
              <Paginacion pagina={pagina} alCambiar={irAPagina} />
            </>
          )
        )}
      </EstadoCarga>
    </section>
  )
}
