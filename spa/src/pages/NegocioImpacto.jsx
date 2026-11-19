import { useAuth } from '../auth/AuthContext.jsx'
import { api } from '../api/client.js'
import EstadoCarga from '../components/EstadoCarga.jsx'
import Icono from '../components/Iconos.jsx'
import useCargar from '../components/useCargar.js'
import { formatearKg } from '../components/Formato.js'

// Insignias de negocio (códigos de CodigoInsignia del backend).
const INSIGNIAS_NEGOCIO = [
  {
    codigo: 'CERO_DESPERDICIO',
    nombre: 'Cero desperdicio',
    descripcion: 'Al menos 10 paquetes cerrados y 80 % o más donados en lugar de perdidos.',
    emoji: '♻️',
  },
]

async function cargarImpacto(negocioId) {
  const [negocio, perfil] = await Promise.all([api.obtenerNegocio(negocioId), api.perfilImpactoNegocio(negocioId)])
  return { negocio, perfil }
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