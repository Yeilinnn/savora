import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { API_URL } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import CampoClave from '../components/CampoClave.jsx'
import { Logo } from '../components/Iconos.jsx'

export default function Login() {
  const { sesion, iniciarSesion } = useAuth()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  if (sesion) return <Navigate to="/" replace />

  async function enviar(evento) {
    evento.preventDefault()
    setError(null)
    setEnviando(true)
    try {
      await iniciarSesion(username.trim(), password)
      navegar('/', { replace: true })
    } catch (e) {
      setError(e.estado === 401 ? 'Usuario o contraseña incorrectos.' : e.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className="login">
      <section className="login-portada">
        <div className="login-marca">
          <Logo tamano={44} />
          <span>Savora</span>
        </div>
        <p className="etiqueta-seccion">Comida rica, planeta feliz</p>
        <h1>
          Rescata algo <span>delicioso</span> hoy.
        </h1>
        <p className="sub">Paquetes sorpresa de tus negocios favoritos, a un precio que te va a encantar.</p>
      </section>

      <form className="tarjeta" onSubmit={enviar}>
        <h2>Inicia sesión</h2>
        <p className="sub">Clientes y negocios entran por aquí.</p>

        {ubicacion.state?.vencida && (
          <p className="alerta" role="alert">
            Tu sesión venció. Inicia sesión de nuevo.
          </p>
        )}
        {ubicacion.state?.clave && (
          <p className="alerta ok" role="status">
            Contraseña actualizada. Ya puedes iniciar sesión.
          </p>
        )}
        {error && (
          <p className="alerta error" role="alert">
            {error}
          </p>
        )}

        <label htmlFor="username">Usuario</label>
        <input
          id="username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoComplete="username"
          autoCapitalize="none"
          required
        />

        <label htmlFor="password">Contraseña</label>
        <CampoClave
          id="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
        />

        <p className="enlace-olvide">
          <Link to="/olvide">¿Olvidó su contraseña?</Link>
        </p>

        <button type="submit" className="primario" disabled={enviando}>
          {enviando ? 'Ingresando…' : 'Ingresar'}
        </button>

        <p className="pie-formulario">
          ¿Aún no tienes cuenta? <Link to="/registro">Crear cuenta de cliente</Link>
        </p>
        <p className="pie-formulario">
          ¿Tiene un negocio? <Link to="/registro-negocio">Registre su negocio</Link>
        </p>
        <p className="pie-formulario">
          <a href={`${API_URL}/login`} target="_blank" rel="noopener noreferrer">
            Acceso administradores
          </a>
        </p>
      </form>
    </main>
  )
}