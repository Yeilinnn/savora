import { useState } from 'react'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import CampoClave from '../components/CampoClave.jsx'
import { Logo } from '../components/Iconos.jsx'

export default function RestablecerContrasena() {
  const { sesion } = useAuth()
  const navegar = useNavigate()
  const [parametros] = useSearchParams()
  const token = parametros.get('token') || ''
  const [password, setPassword] = useState('')
  const [confirmar, setConfirmar] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  if (sesion) return <Navigate to="/" replace />

  async function enviar(evento) {
    evento.preventDefault()
    if (password.length < 8 || password.length > 72) {
      setError('La contraseña debe tener entre 8 y 72 caracteres.')
      return
    }
    if (password !== confirmar) {
      setError('Las contraseñas no coinciden.')
      return
    }
    setError(null)
    setEnviando(true)
    try {
      await api.restablecerContrasena(token, password)
      navegar('/login', { replace: true, state: { clave: true } })
    } catch (e) {
      setError(e.message)
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
        <p className="etiqueta-seccion">Casi listo</p>
        <h1>
          Elige una contraseña <span>nueva</span>.
        </h1>
        <p className="sub">Úsela para iniciar sesión desde ahora.</p>
      </section>

      <form className="tarjeta" onSubmit={enviar} noValidate>
        <h2>Nueva contraseña</h2>

        {!token ? (
          <>
            <p className="alerta error" role="alert">
              Este enlace está incompleto. Solicite uno nuevo.
            </p>
            <p className="pie-formulario">
              <Link to="/olvide">Solicitar enlace nuevo</Link>
            </p>
          </>
        ) : (
          <>
            {error && (
              <p className="alerta error" role="alert">
                {error}
              </p>
            )}
            <label htmlFor="clave-restablecer">Contraseña nueva</label>
            <CampoClave
              id="clave-restablecer"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="new-password"
              placeholder="Mínimo 8 caracteres"
            />
            <label htmlFor="clave-restablecer-2">Repita la contraseña</label>
            <CampoClave
              id="clave-restablecer-2"
              value={confirmar}
              onChange={(e) => setConfirmar(e.target.value)}
              autoComplete="new-password"
            />
            <button type="submit" className="primario" disabled={enviando}>
              {enviando ? 'Guardando…' : 'Guardar contraseña'}
            </button>
            <p className="pie-formulario">
              <Link to="/olvide">¿El enlace venció? Solicitar otro</Link>
            </p>
          </>
        )}
      </form>
    </main>
  )
}