import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { api } from '../api/client.js'
import EstadoCarga from '../components/EstadoCarga.jsx'
import Icono from '../components/Iconos.jsx'
import useCargar from '../components/useCargar.js'
import { INSIGNIAS } from '../components/insignias.js'

async function cargarPerfil(clienteId) {
  const [cliente, perfil] = await Promise.all([api.obtenerCliente(clienteId), api.perfilImpacto(clienteId)])
  return { cliente, perfil }
}

export default function Perfil() {
  const { sesion, cerrarSesion } = useAuth()
  const { estado, datos, error, reintentar } = useCargar(() => cargarPerfil(sesion.clienteId), [sesion.clienteId])

  const obtenidas = new Set(datos?.perfil?.insignias ?? [])
  const inicial = (datos?.cliente?.nombre || sesion.username || '?').charAt(0).toUpperCase()

  return (
    <section>
      <p className="etiqueta-seccion naranja">Tu cuenta</p>
      <h1>Mi perfil</h1>

      <EstadoCarga estado={estado} error={error} alReintentar={reintentar}>
        {datos && (
          <>
            <div className="perfil-cabecera">
              <span className="avatar grande" aria-hidden="true">
                {inicial}
              </span>
              <div>
                <h2>{datos.cliente.nombre}</h2>
                <p className="negocio">@{sesion.username} · Cliente</p>
              </div>
            </div>

            <ul className="datos-perfil">
              <li>
                <Icono nombre="correo" />
                <div>
                  <span>Correo</span>
                  <strong>{datos.cliente.correo || 'Sin registrar'}</strong>
                </div>
              </li>
              <li>
                <Icono nombre="telefono" />
                <div>
                  <span>Teléfono</span>
                  <strong>{datos.cliente.telefono || 'Sin registrar'}</strong>
                </div>
              </li>
              <li>
                <Icono nombre="caja" />
                <div>
                  <span>Paquetes rescatados</span>
                  <strong>{datos.perfil.reservasRecogidas}</strong>
                </div>
              </li>
              <li>
                <Icono nombre="hoja" />
                <div>
                  <span>Insignias logradas</span>
                  <strong>
                    {INSIGNIAS.filter((i) => obtenidas.has(i.codigo)).length} de {INSIGNIAS.length}
                  </strong>
                </div>
              </li>
            </ul>

            <Link to="/impacto" className="enlace-boton">
              Ver mi impacto e insignias
            </Link>
          </>
        )}
      </EstadoCarga>

      <button type="button" className="peligro" onClick={cerrarSesion}>
        <Icono nombre="salir" /> Cerrar sesión
      </button>
    </section>
  )
}