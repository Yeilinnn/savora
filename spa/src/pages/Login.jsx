import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'

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
      <form className="tarjeta" onSubmit={enviar}>
        <h1>
          Savora <span>rescata comida</span>
        </h1>
        <p className="sub">Inicie sesión para reservar paquetes sorpresa.</p>

        {ubicacion.state?.vencida && (
          <p className="alerta" role="alert">
            Su sesión venció. Inicie sesión de nuevo.
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
          required
        />

        <label htmlFor="password">Contraseña</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
          required
        />

        <button type="submit" className="primario" disabled={enviando}>
          {enviando ? 'Ingresando…' : 'Ingresar'}
        </button>
      </form>
    </main>
  )
}
