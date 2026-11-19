import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { api } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { Logo } from '../components/Iconos.jsx'

export default function OlvideContrasena() {
  const { sesion } = useAuth()
  const [correo, setCorreo] = useState('')
  const [error, setError] = useState(null)
  const [enviado, setEnviado] = useState(false)
  const [enviando, setEnviando] = useState(false)

  if (sesion) return <Navigate to="/" replace />

  async function enviar(evento) {
    evento.preventDefault()
    setError(null)
    setEnviando(true)
    try {
      await api.olvideContrasena(correo.trim())
      setEnviado(true)
    } catch (e) {
      setError(e.estado === 400 ? 'Escribe un correo válido.' : e.message)
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
        <p className="etiqueta-seccion">Recupera tu acceso</p>
        <h1>
          Te ayudamos a <span>volver a entrar</span>.
        </h1>
        <p className="sub">Te enviamos un enlace al correo de tu cuenta para elegir una contraseña nueva.</p>
      </section>

      <form className="tarjeta" onSubmit={enviar} noValidate>
        <h2>¿Olvidó su contraseña?</h2>

        {enviado ? (
          <p className="alerta ok" role="status">
            Si el correo está registrado, le enviamos un enlace para restablecer la contraseña. Revise también la
            carpeta de spam. El enlace vale 30 minutos.
          </p>
        ) : (
          <>
            <p className="sub">Escriba el correo con el que creó su cuenta.</p>
            {error && (
              <p className="alerta error" role="alert">
                {error}
              </p>
            )}
            <label htmlFor="correo-olvide">Correo</label>
            <input
              id="correo-olvide"
              type="email"
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
              autoComplete="email"
              required
            />
            <button type="submit" className="primario" disabled={enviando}>
              {enviando ? 'Enviando…' : 'Enviar enlace'}
            </button>
          </>
        )}

        <p className="pie-formulario">
          <Link to="/login">Volver a iniciar sesión</Link>
        </p>
      </form>
    </main>
  )
}