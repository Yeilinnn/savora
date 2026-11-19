import { useState } from 'react'
import { api, EVENTO_RESERVAS_CAMBIARON } from '../api/client.js'
import EstadoCarga from '../components/EstadoCarga.jsx'
import Icono from '../components/Iconos.jsx'
import Paginacion from '../components/Paginacion.jsx'
import usePagina from '../components/usePagina.js'
import { visualDe } from '../components/categorias.js'
import { formatearFechaHora, formatearMoneda } from '../components/Formato.js'

const TAMANO = 10

// Funciones estables (fuera del componente) para que usePagina no recargue en cada render.
const SOLO_PENDIENTES = (pagina, tamano) => api.listarReservasNegocio(pagina, tamano, 'pendiente')
const TODAS = (pagina, tamano) => api.listarReservasNegocio(pagina, tamano)

function etiquetaEstado(estado) {
  const texto = (estado || '').replace(/_/g, ' ')
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

function Lista({ cargar }) {
  const { estado, pagina, error, irAPagina, reintentar } = usePagina(cargar, TAMANO)
  const [aviso, setAviso] = useState(null)
  const [trabajando, setTrabajando] = useState(null)

  async function ejecutar(reserva, accion, texto) {
    setAviso(null)
    setTrabajando(reserva.id)
    try {
      await accion(reserva.id)
      setAviso({ tipo: 'ok', texto })
      window.dispatchEvent(new Event(EVENTO_RESERVAS_CAMBIARON))
      reintentar()
    } catch (e) {
      setAviso({ tipo: 'error', texto: e.message })
    } finally {
      setTrabajando(null)
    }
  }

  const ahora = Date.now()

  return (
    <>
      {aviso && (
        <p className={`alerta ${aviso.tipo === 'error' ? 'error' : 'ok'}`} role="status">
          {aviso.texto}
        </p>
      )}
      <EstadoCarga estado={estado} error={error} alReintentar={reintentar}>
        {pagina && pagina.contenido.length === 0 ? (
          <p className="estado">No hay reservas para mostrar.</p>
        ) : (
          pagina && (
            <>
              <ul className="lista-reservas">
                {pagina.contenido.map((r) => {
                  const visual = visualDe(r.descripcionPaquete)
                  const estadoReserva = (r.estado || '').toLowerCase()
                  const pendiente = estadoReserva === 'pendiente'
                  const vencida = new Date(r.horaLimiteRecogida).getTime() < ahora
                  const ocupado = trabajando !== null
                  return (
                    <li key={r.id} className="reserva">
                      <div className={`reserva-imagen ${visual.clase}`} aria-hidden="true">
                        {visual.emoji}
                      </div>
                      <div className="reserva-datos">
                        <p className="etiqueta-seccion">{formatearFechaHora(r.fechaHoraReserva)}</p>
                        <h3>{r.descripcionPaquete}</h3>
                        <p className="negocio">
                          <Icono nombre="usuario" tamano={14} /> {r.nombreCliente} · {formatearMoneda(r.precioConDescuento)}
                        </p>
                        <p className="meta">
                          <Icono nombre="reloj" tamano={14} /> Recoger antes de {formatearFechaHora(r.horaLimiteRecogida)}
                        </p>
                        {pendiente && (
                          <div className="acciones">
                            <button
                              type="button"
                              className="chico primario"
                              onClick={() => ejecutar(r, api.marcarRecogida, `Entregado a ${r.nombreCliente}.`)}
                              disabled={ocupado}
                            >
                              {trabajando === r.id ? 'Guardando…' : 'Marcar recogida'}
                            </button>
                            <button
                              type="button"
                              className="chico"
                              onClick={() =>
                                ejecutar(r, api.marcarNoRecogida, `Se registró que ${r.nombreCliente} no recogió el paquete.`)
                              }
                              disabled={ocupado || !vencida}
                              title={vencida ? undefined : 'Disponible cuando pase la hora límite'}
                            >
                              No la recogió
                            </button>
                          </div>
                        )}
                      </div>
                      <div className="reserva-lado">
                        <span className={`insignia-estado estado-${estadoReserva}`}>{etiquetaEstado(r.estado)}</span>
                      </div>
                    </li>
                  )
                })}
              </ul>
              <Paginacion pagina={pagina} alCambiar={irAPagina} />
            </>
          )
        )}
      </EstadoCarga>
    </>
  )
}

export default function NegocioReservas() {
  const [verTodas, setVerTodas] = useState(false)

  return (
    <section>
      <p className="etiqueta-seccion naranja">Tu negocio</p>
      <h1>Reservas recibidas</h1>
      <p className="sub">Cuando el cliente llegue, marca el paquete como recogido.</p>

      <div className="filtros" role="group" aria-label="Filtrar reservas">
        <button type="button" className={`filtro ${!verTodas ? 'activo' : ''}`} aria-pressed={!verTodas} onClick={() => setVerTodas(false)}>
          Pendientes
        </button>
        <button type="button" className={`filtro ${verTodas ? 'activo' : ''}`} aria-pressed={verTodas} onClick={() => setVerTodas(true)}>
          Todas
        </button>
      </div>

      <Lista key={verTodas ? 'todas' : 'pendientes'} cargar={verTodas ? TODAS : SOLO_PENDIENTES} />
    </section>
  )
}