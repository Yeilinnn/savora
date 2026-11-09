import { useAuth } from '../auth/AuthContext.jsx'
import { api } from '../api/client.js'
import EstadoCarga from '../components/EstadoCarga.jsx'
import Icono from '../components/Iconos.jsx'
import useCargar from '../components/useCargar.js'
import { INSIGNIAS } from '../components/insignias.js'
import { ahorroDeReserva, formatearKg, formatearMoneda } from '../components/Formato.js'

async function cargarImpacto(clienteId) {
  const [cliente, perfil] = await Promise.all([api.obtenerCliente(clienteId), api.perfilImpacto(clienteId)])
  // El ahorro se calcula con las reservas ya recogidas (la API de impacto no lo trae).
  let ahorro = null
  try {
    const recogidas = await api.listarReservas(0, 100, 'recogido')
    ahorro = recogidas.contenido.reduce((suma, r) => suma + ahorroDeReserva(r), 0)
  } catch {
    ahorro = null
  }
  return { cliente, perfil, ahorro }
}

export default function Impacto() {
  const { sesion } = useAuth()
  const { estado, datos, error, reintentar } = useCargar(() => cargarImpacto(sesion.clienteId), [sesion.clienteId])

  const nombre = datos?.cliente?.nombre?.split(' ')[0]
  const recogidas = datos?.perfil?.reservasRecogidas ?? 0
  const obtenidas = new Set(datos?.perfil?.insignias ?? [])

  return (
    <section>
      <p className="etiqueta-seccion naranja">Cada elección cuenta</p>
      <h1>Mi impacto</h1>
      <p className="sub">Lo que has logrado al elegir rescatar en lugar de desperdiciar.</p>

      <EstadoCarga estado={estado} error={error} alReintentar={reintentar}>
        {datos && (
          <>
            <div className="impacto-resumen">
              <span className="impacto-icono" aria-hidden="true">
                <Icono nombre="hoja" tamano={30} />
              </span>
              <div>
                <p className="impacto-pequeno">Tu impacto hasta hoy</p>
                <h2>{recogidas > 0 ? `¡Vas muy bien, ${nombre}!` : `¡Hola, ${nombre}!`}</h2>
                <p>
                  {recogidas > 0
                    ? 'Gracias por ser parte del cambio en Costa Rica.'
                    : 'Recoge tu primer paquete sorpresa y empieza a sumar impacto.'}
                </p>
              </div>
            </div>

            <ul className="estadisticas">
              <li>
                <Icono nombre="caja" />
                <strong>{recogidas}</strong>
                <span>{recogidas === 1 ? 'paquete rescatado' : 'paquetes rescatados'}</span>
              </li>
              <li>
                <Icono nombre="corazon" />
                <strong>{datos.ahorro === null ? '—' : formatearMoneda(datos.ahorro)}</strong>
                <span>ahorrados</span>
              </li>
              <li>
                <Icono nombre="hoja" />
                <strong>{formatearKg(datos.perfil.kilogramosRescatados)}</strong>
                <span>de comida salvada</span>
              </li>
              <li>
                <Icono nombre="reloj" />
                <strong>{datos.perfil.limiteReservasActivas}</strong>
                <span>reservas activas permitidas</span>
              </li>
            </ul>

            <h2 className="titulo-lista">Mis insignias</h2>
            <ul className="insignias">
              {INSIGNIAS.map((i) => {
                const lograda = obtenidas.has(i.codigo)
                return (
                  <li key={i.codigo} className={`insignia ${lograda ? 'lograda' : 'bloqueada'}`}>
                    <span className="insignia-emoji" aria-hidden="true">
                      {lograda ? i.emoji : <Icono nombre="candado" tamano={22} />}
                    </span>
                    <div>
                      <h3>{i.nombre}</h3>
                      <p>{i.descripcion}</p>
                      <span className="insignia-estado-texto">
                        {lograda ? 'Lograda' : i.disponible ? 'Por lograr' : 'Próximamente'}
                      </span>
                    </div>
                  </li>
                )
              })}
            </ul>
          </>
        )}
      </EstadoCarga>
    </section>
  )
}