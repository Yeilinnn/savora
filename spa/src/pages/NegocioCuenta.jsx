import { useAuth } from '../auth/AuthContext.jsx'
import { api } from '../api/client.js'
import CambiarContrasena from '../components/CambiarContrasena.jsx'
import EstadoCarga from '../components/EstadoCarga.jsx'
import Icono from '../components/Iconos.jsx'
import useCargar from '../components/useCargar.js'

export default function NegocioCuenta() {
  const { sesion, cerrarSesion } = useAuth()
  const { estado, datos, error, reintentar } = useCargar(() => api.obtenerNegocio(sesion.negocioId), [sesion.negocioId])

  return (
    <section>
      <p className="etiqueta-seccion naranja">Tu cuenta</p>
      <h1>Mi negocio</h1>

      <EstadoCarga estado={estado} error={error} alReintentar={reintentar}>
        {datos && (
          <>
            <div className="perfil-cabecera">
              <span className="avatar grande" aria-hidden="true">
                {datos.nombre.charAt(0).toUpperCase()}
              </span>
              <div>
                <h2>{datos.nombre}</h2>
                <p className="negocio">@{sesion.username} · Negocio</p>
              </div>
            </div>

            <ul className="datos-perfil">
              <li>
                <Icono nombre="bolsa" />
                <div>
                  <span>Tipo de negocio</span>
                  <strong>{datos.tipoNegocio || 'Sin registrar'}</strong>
                </div>
              </li>
              <li>
                <Icono nombre="usuario" />
                <div>
                  <span>Ubicación</span>
                  <strong>{datos.ubicacion || 'Sin registrar'}</strong>
                </div>
              </li>
              <li>
                <Icono nombre="reloj" />
                <div>
                  <span>Horario de cierre</span>
                  <strong>{datos.horarioCierre ? String(datos.horarioCierre).slice(0, 5) : 'Sin registrar'}</strong>
                </div>
              </li>
            </ul>
          </>
        )}
      </EstadoCarga>

      <CambiarContrasena />

      <button type="button" className="peligro" onClick={cerrarSesion}>
        <Icono nombre="salir" /> Cerrar sesión
      </button>
    </section>
  )
}