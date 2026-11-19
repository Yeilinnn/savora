import { useEffect, useMemo, useState } from 'react'
import { useAuth } from '../auth/AuthContext.jsx'
import { api } from '../api/client.js'
import EstadoCarga from '../components/EstadoCarga.jsx'
import Icono from '../components/Iconos.jsx'
import Paginacion from '../components/Paginacion.jsx'
import usePagina from '../components/usePagina.js'
import { visualDe } from '../components/categorias.js'
import { formatearFechaHora, formatearMoneda, porcentajeDescuento } from '../components/Formato.js'

const TAMANO = 10
const VACIO = { categoriaId: '', descripcion: '', cantidad: '1', precioOriginal: '', precioConDescuento: '', horaLimite: '' }

function etiquetaEstado(estado) {
  const texto = (estado || '').replace(/_/g, ' ')
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

function textoDeCierre(resumen) {
  if (resumen.resultado === 'DONADO') {
    return `Paquete donado a ${resumen.nombreOrganizacion} (${resumen.kilogramosDonados} kg). ¡Gracias por no desperdiciar!`
  }
  return 'Paquete cerrado: ninguna organización pudo recibirlo, se registró como perdido.'
}

function validar(d) {
  if (!d.categoriaId) return 'Elige una categoría.'
  if (!d.descripcion.trim()) return 'Escribe una descripción.'
  if (!(Number(d.cantidad) >= 1)) return 'La cantidad debe ser al menos 1.'
  const original = Number(d.precioOriginal)
  const final = Number(d.precioConDescuento)
  if (!(original > 0)) return 'El precio original debe ser mayor que cero.'
  if (!(final >= 0)) return 'El precio con descuento no puede ser negativo.'
  if (final >= original) return 'El precio con descuento debe ser menor que el original.'
  if (!d.horaLimite) return 'Indica la hora límite de recogida.'
  if (new Date(d.horaLimite) <= new Date()) return 'La hora límite debe ser en el futuro.'
  return null
}

function FormularioPaquete({ alPublicar }) {
  const [categorias, setCategorias] = useState([])
  const [datos, setDatos] = useState(VACIO)
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    api
      .listarCategorias()
      .then(setCategorias)
      .catch(() => setError('No se pudieron cargar las categorías.'))
  }, [])

  const cambiar = (campo) => (e) => setDatos({ ...datos, [campo]: e.target.value })

  async function enviar(evento) {
    evento.preventDefault()
    const problema = validar(datos)
    if (problema) {
      setError(problema)
      return
    }
    setError(null)
    setEnviando(true)
    try {
      const creado = await api.crearPaquete({
        categoriaId: Number(datos.categoriaId),
        descripcion: datos.descripcion.trim(),
        cantidad: Number(datos.cantidad),
        precioOriginal: Number(datos.precioOriginal),
        precioConDescuento: Number(datos.precioConDescuento),
        horaLimiteRecogida: new Date(datos.horaLimite).toISOString(),
      })
      setDatos(VACIO)
      alPublicar(creado)
    } catch (e) {
      setError(e.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="tarjeta formulario-paquete" onSubmit={enviar} noValidate>
      <h2>Publicar paquete sorpresa</h2>
      <p className="sub">Los clientes lo verán en Explorar apenas lo publiques.</p>

      {error && (
        <p className="alerta error" role="alert">
          {error}
        </p>
      )}

      <label htmlFor="p-categoria">Categoría</label>
      <select id="p-categoria" value={datos.categoriaId} onChange={cambiar('categoriaId')} required>
        <option value="">Elige una categoría</option>
        {categorias.map((c) => (
          <option key={c.id} value={c.id}>
            {c.nombre}
          </option>
        ))}
      </select>

      <label htmlFor="p-descripcion">Descripción</label>
      <input
        id="p-descripcion"
        value={datos.descripcion}
        onChange={cambiar('descripcion')}
        placeholder="ej. Caja de pan y repostería del día"
        required
      />

      <div className="fila-campos">
        <div>
          <label htmlFor="p-cantidad">Cantidad</label>
          <input id="p-cantidad" type="number" min="1" step="1" value={datos.cantidad} onChange={cambiar('cantidad')} required />
        </div>
        <div>
          <label htmlFor="p-original">Precio original (₡)</label>
          <input
            id="p-original"
            type="number"
            min="1"
            step="any"
            value={datos.precioOriginal}
            onChange={cambiar('precioOriginal')}
            required
          />
        </div>
        <div>
          <label htmlFor="p-descuento">Precio con descuento (₡)</label>
          <input
            id="p-descuento"
            type="number"
            min="0"
            step="any"
            value={datos.precioConDescuento}
            onChange={cambiar('precioConDescuento')}
            required
          />
        </div>
      </div>

      <label htmlFor="p-limite">Hora límite de recogida</label>
      <input id="p-limite" type="datetime-local" value={datos.horaLimite} onChange={cambiar('horaLimite')} required />

      <button type="submit" className="primario" disabled={enviando}>
        {enviando ? 'Publicando…' : 'Publicar paquete'}
      </button>
    </form>
  )
}

export default function NegocioPaquetes() {
  const { sesion } = useAuth()
  const cargar = useMemo(() => api.listarPaquetesNegocio(sesion.negocioId), [sesion.negocioId])
  const { estado, pagina, error, irAPagina, reintentar } = usePagina(cargar, TAMANO)
  const [formAbierto, setFormAbierto] = useState(false)
  const [aviso, setAviso] = useState(null)
  const [cerrando, setCerrando] = useState(null)

  function alPublicar(creado) {
    setFormAbierto(false)
    setAviso({ tipo: 'ok', texto: `Publicaste: ${creado.descripcion}` })
    irAPagina(0)
    reintentar()
  }

  async function cerrar(paquete) {
    setAviso(null)
    setCerrando(paquete.id)
    try {
      const resumen = await api.cerrarPaquete(paquete.id)
      setAviso({ tipo: 'ok', texto: textoDeCierre(resumen) })
      reintentar()
    } catch (e) {
      setAviso({ tipo: 'error', texto: e.message })
    } finally {
      setCerrando(null)
    }
  }

  const ahora = Date.now()

  return (
    <section>
      <p className="etiqueta-seccion naranja">Tu negocio</p>
      <h1>Mis paquetes</h1>
      <p className="sub">Publica paquetes sorpresa y cierra los que ya vencieron para donarlos.</p>

      <button type="button" className="primario ancho" onClick={() => setFormAbierto(!formAbierto)} aria-expanded={formAbierto}>
        <Icono nombre={formAbierto ? 'cerrar' : 'caja'} tamano={18} /> {formAbierto ? 'Cancelar' : 'Publicar paquete nuevo'}
      </button>

      {formAbierto && <FormularioPaquete alPublicar={alPublicar} />}

      {aviso && (
        <p className={`alerta ${aviso.tipo === 'error' ? 'error' : 'ok'} separada`} role="status">
          {aviso.texto}
        </p>
      )}

      <EstadoCarga estado={estado} error={error} alReintentar={reintentar}>
        {pagina && pagina.contenido.length === 0 ? (
          <p className="estado">Todavía no has publicado paquetes.</p>
        ) : (
          pagina && (
            <>
              <ul className="lista-reservas separada">
                {pagina.contenido.map((p) => {
                  const visual = visualDe(`${p.nombreCategoria} ${p.descripcion}`)
                  const descuento = porcentajeDescuento(p.precioOriginal, p.precioConDescuento)
                  const vencido = new Date(p.horaLimiteRecogida).getTime() < ahora
                  const puedeCerrar = p.estado === 'disponible' && vencido
                  return (
                    <li key={p.id} className="reserva">
                      <div className={`reserva-imagen ${visual.clase}`} aria-hidden="true">
                        {visual.emoji}
                      </div>
                      <div className="reserva-datos">
                        <p className="etiqueta-seccion">{p.nombreCategoria}</p>
                        <h3>{p.descripcion}</h3>
                        <p className="negocio">
                          {formatearMoneda(p.precioConDescuento)} <s className="tachado">{formatearMoneda(p.precioOriginal)}</s>
                          {descuento > 0 && ` · -${descuento}%`} · {p.cantidad === 1 ? '1 unidad' : `${p.cantidad} unidades`}
                        </p>
                        <p className="meta">
                          <Icono nombre="reloj" tamano={14} /> Límite: {formatearFechaHora(p.horaLimiteRecogida)}
                          {vencido && p.estado === 'disponible' ? ' (vencido)' : ''}
                        </p>
                        {puedeCerrar && (
                          <button type="button" className="chico" onClick={() => cerrar(p)} disabled={cerrando !== null}>
                            {cerrando === p.id ? 'Cerrando…' : 'Cerrar y donar'}
                          </button>
                        )}
                      </div>
                      <div className="reserva-lado">
                        <span className={`insignia-estado estado-${p.estado}`}>{etiquetaEstado(p.estado)}</span>
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
    </section>
  )
}