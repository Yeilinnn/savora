import { useAuth } from '../auth/AuthContext.jsx'
import { api } from '../api/client.js'
import EstadoCarga from '../components/EstadoCarga.jsx'
import Estrellas from '../components/Estrellas.jsx'
import Icono from '../components/Iconos.jsx'
import useCargar from '../components/useCargar.js'
import { formatearKg } from '../components/Formato.js'
import { INSIGNIAS_NEGOCIO, textoRacha } from '../components/insignias.js'
import '../components/resenas.css'

async function cargarImpacto(negocioId) {
  const [negocio, perfil, resumenResenas, resenas] = await Promise.all([
    api.obtenerNegocio(negocioId),
    api.perfilImpactoNegocio(negocioId),
    api.resumenResenasNegocio(),
    api.listarResenasNegocio(0, 5),
  ])
  return { negocio, perfil, resumenResenas, resenas: resenas.contenido }
}

export default function NegocioImpacto() {
  const { sesion } = useAuth()
  const { estado, datos, error, reintentar } = useCargar(() => cargarImpacto(sesion.negocioId), [sesion.negocioId])
  const obtenidas = new Set(datos?.perfil?.insignias ?? [])

  return (
    <section>
      <p className="etiqueta-seccion naranja">Tu negocio</p>
      <h1>Impacto del negocio</h1>
      <p className="sub">Cómo tu negocio reduce el desperdicio de comida.</p>

      <EstadoCarga estado={estado} error={error} alReintentar={reintentar}>
        {datos && (
          <>
            <div className="impacto-resumen">
              <span className="impacto-icono" aria-hidden="true">
                <Icono nombre="hoja" tamano={30} />
              </span>
              <div>
                <p className="impacto-pequeno">{datos.negocio.nombre}</p>
                <h2>{datos.perfil.reservasRecogidas > 0 ? '¡Tu comida está llegando a la mesa!' : '¡Bienvenido a Savora!'}</h2>
                <p>
                  {datos.perfil.reservasRecogidas > 0
                    ? 'Cada paquete recogido es comida que no se desperdició.'
                    : 'Publica tu primer paquete y empieza a sumar impacto.'}
                </p>
              </div>
            </div>

            <ul className="estadisticas">
              <li>
                <Icono nombre="caja" />
                <strong>{datos.perfil.reservasRecogidas}</strong>
                <span>paquetes recogidos</span>
              </li>
              <li>
                <Icono nombre="reloj" />
                <strong>{datos.perfil.reservasNoRecogidas}</strong>
                <span>no recogidos</span>
              </li>
              <li>
                <Icono nombre="corazon" />
                <strong>{datos.perfil.paquetesDonados}</strong>
                <span>paquetes donados</span>
              </li>
              <li>
                <Icono nombre="hoja" />
                <strong>{formatearKg(datos.perfil.kilogramosRescatados)}</strong>
                <span>de comida salvada</span>
              </li>
            </ul>

            <h2 className="titulo-lista">Racha del negocio</h2>
            <ul className="insignias">
              <li className={`insignia ${datos.perfil.rachaActualSemanas > 0 ? 'lograda' : 'bloqueada'}`}>
                <span className="insignia-emoji" aria-hidden="true">
                  🔥
                </span>
                <div>
                  <h3>
                    {datos.perfil.rachaActualSemanas} {datos.perfil.rachaActualSemanas === 1 ? 'semana' : 'semanas'} seguidas
                  </h3>
                  <p>{textoRacha(datos.perfil.rachaActualSemanas, datos.perfil.mejorRachaSemanas, true)}</p>
                </div>
              </li>
            </ul>

            <h2 className="titulo-lista">Reseñas de tus clientes</h2>
            {datos.resumenResenas.total === 0 ? (
              <p className="estado">Todavía no tienes reseñas. Aparecen cuando un cliente califica un paquete recogido.</p>
            ) : (
              <>
                <p>
                  <Estrellas valor={datos.resumenResenas.promedio} /> <strong>{datos.resumenResenas.promedio.toFixed(1)}</strong>{' '}
                  de 5 ({datos.resumenResenas.total} {datos.resumenResenas.total === 1 ? 'reseña' : 'reseñas'})
                </p>
                <ul className="resena-lista">
                  {datos.resenas.map((r) => (
                    <li key={r.id} className="resena-item">
                      <div className="resena-cabecera">
                        <span>
                          <Estrellas valor={r.calificacion} /> {r.nombreCliente}
                        </span>
                        <span>{new Date(r.fecha).toLocaleDateString('es-CR')}</span>
                      </div>
                      {r.comentario && <p>{r.comentario}</p>}
                    </li>
                  ))}
                </ul>
              </>
            )}

            <h2 className="titulo-lista">Insignias del negocio</h2>
            <ul className="insignias">
              {INSIGNIAS_NEGOCIO.map((i) => {
                const lograda = obtenidas.has(i.codigo)
                return (
                  <li key={i.codigo} className={`insignia ${lograda ? 'lograda' : 'bloqueada'}`}>
                    <span className="insignia-emoji" aria-hidden="true">
                      {lograda ? i.emoji : <Icono nombre="candado" tamano={22} />}
                    </span>
                    <div>
                      <h3>{i.nombre}</h3>
                      <p>{i.descripcion}</p>
                      <span className="insignia-estado-texto">{lograda ? 'Lograda' : 'Por lograr'}</span>
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