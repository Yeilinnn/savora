import { useMemo, useState } from 'react'
import { api, EVENTO_RESERVAS_CAMBIARON } from '../api/client.js'
import EstadoCarga from '../components/EstadoCarga.jsx'
import Icono from '../components/Iconos.jsx'
import Paginacion from '../components/Paginacion.jsx'
import usePagina from '../components/usePagina.js'
import { visualDe } from '../components/categorias.js'
import { formatearLimite, formatearMoneda, porcentajeDescuento } from '../components/Formato.js'

const TAMANO = 24

function mensajeDeError(e) {
  if (e.estado === 422) return `No se pudo reservar: ${e.message}`
  if (e.estado === 404) return 'El paquete ya no existe.'
  if (e.estado === 403) return 'Tu cuenta no puede realizar reservas.'
  return e.message
}

function sinTildes(texto) {
  return (texto || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
}

export default function Paquetes() {
  const { estado, pagina, error, irAPagina, reintentar } = usePagina(api.listarPaquetes, TAMANO)
  const [reservando, setReservando] = useState(null)
  const [aviso, setAviso] = useState(null)
  const [categoria, setCategoria] = useState('Todo')
  const [buscando, setBuscando] = useState(false)
  const [texto, setTexto] = useState('')

  const categorias = useMemo(() => {
    const nombres = new Set((pagina?.contenido ?? []).map((p) => p.nombreCategoria))
    return ['Todo', ...nombres]
  }, [pagina])

  const visibles = useMemo(() => {
    const consulta = sinTildes(texto.trim())
    return (pagina?.contenido ?? []).filter((p) => {
      if (categoria !== 'Todo' && p.nombreCategoria !== categoria) return false
      if (!consulta) return true
      return sinTildes(`${p.descripcion} ${p.nombreNegocio} ${p.nombreCategoria}`).includes(consulta)
    })
  }, [pagina, categoria, texto])

  async function reservar(paquete) {
    setAviso(null)
    setReservando(paquete.id)
    try {
      await api.reservar(paquete.id)
      setAviso({ tipo: 'ok', texto: `¡Listo! Reservaste: ${paquete.descripcion}` })
      window.dispatchEvent(new Event(EVENTO_RESERVAS_CAMBIARON))
      reintentar()
    } catch (e) {
      setAviso({ tipo: 'error', texto: mensajeDeError(e) })
    } finally {
      setReservando(null)
    }
  }

  function alternarBusqueda() {
    if (buscando) setTexto('')
    setBuscando(!buscando)
  }

  return (
    <section>
      <div className="portada">
        <p className="etiqueta-seccion">
          <Icono nombre="chispa" tamano={14} /> Comida rica, planeta feliz
        </p>
        <h1>
          Rescata algo <span>delicioso</span> hoy.
        </h1>
        <p>Paquetes sorpresa de tus negocios favoritos, a un precio que te va a encantar.</p>
        <span className="portada-adorno" aria-hidden="true">
          🛍️
        </span>
      </div>

      <div className="encabezado-seccion">
        <div>
          <p className="etiqueta-seccion naranja">Cerca de ti</p>
          <h2 className="titulo-seccion">Encuentra tu próximo rescate</h2>
        </div>
        <button
          type="button"
          className="icono-boton"
          onClick={alternarBusqueda}
          aria-label={buscando ? 'Cerrar búsqueda' : 'Buscar paquetes'}
          aria-expanded={buscando}
        >
          <Icono nombre={buscando ? 'cerrar' : 'buscar'} />
        </button>
      </div>

      {buscando && (
        <input
          type="search"
          className="buscador"
          placeholder="Busca por paquete, negocio o categoría"
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          aria-label="Buscar paquetes"
          autoFocus
        />
      )}

      {aviso && (
        <p className={`alerta ${aviso.tipo === 'error' ? 'error' : 'ok'}`} role="status">
          {aviso.texto}
        </p>
      )}

      <EstadoCarga estado={estado} error={error} alReintentar={reintentar}>
        {pagina && pagina.contenido.length === 0 ? (
          <p className="estado">Hoy no hay paquetes disponibles. ¡Vuelve más tarde!</p>
        ) : (
          pagina && (
            <>
              <div className="filtros" role="group" aria-label="Filtrar por categoría">
                {categorias.map((c) => (
                  <button
                    key={c}
                    type="button"
                    className={`filtro ${categoria === c ? 'activo' : ''}`}
                    aria-pressed={categoria === c}
                    onClick={() => setCategoria(c)}
                  >
                    {c === 'Todo' ? (
                      <Icono nombre="chispa" tamano={16} />
                    ) : (
                      <span aria-hidden="true">{visualDe(c).emoji}</span>
                    )}
                    {c}
                  </button>
                ))}
              </div>

              {visibles.length === 0 ? (
                <p className="estado">No encontramos paquetes con ese filtro.</p>
              ) : (
                <ul className="rejilla">
                  {visibles.map((p) => {
                    const visual = visualDe(`${p.nombreCategoria} ${p.descripcion}`)
                    const descuento = porcentajeDescuento(p.precioOriginal, p.precioConDescuento)
                    return (
                      <li key={p.id} className="paquete">
                        <div className={`paquete-imagen ${visual.clase}`}>
                          {descuento > 0 && <span className="descuento">-{descuento}%</span>}
                          <span className="paquete-emoji" aria-hidden="true">
                            {visual.emoji}
                          </span>
                        </div>
                        <div className="paquete-cuerpo">
                          <span className="categoria">{p.nombreCategoria}</span>
                          <h3>{p.descripcion}</h3>
                          <p className="negocio">{p.nombreNegocio}</p>
                          <p className="precios">
                            <span className="precio">{formatearMoneda(p.precioConDescuento)}</span>
                            <s className="tachado">{formatearMoneda(p.precioOriginal)}</s>
                          </p>
                          <p className="meta">
                            <Icono nombre="reloj" tamano={14} /> Recoger antes de {formatearLimite(p.horaLimiteRecogida)}
                          </p>
                          <p className="meta">
                            <Icono nombre="caja" tamano={14} /> {p.cantidad === 1 ? 'Queda 1' : `Quedan ${p.cantidad}`}
                          </p>
                          <button
                            type="button"
                            className="primario"
                            onClick={() => reservar(p)}
                            disabled={reservando !== null}
                          >
                            {reservando === p.id ? 'Reservando…' : 'Reservar'}
                          </button>
                        </div>
                      </li>
                    )
                  })}
                </ul>
              )}
              <Paginacion pagina={pagina} alCambiar={irAPagina} />
            </>
          )
        )}
      </EstadoCarga>
    </section>
  )
}