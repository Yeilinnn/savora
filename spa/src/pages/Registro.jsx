import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import CampoClave from '../components/CampoClave.jsx'
import { Logo } from '../components/Iconos.jsx'

const VACIO = { nombre: '', correo: '', telefono: '', username: '', password: '', confirmar: '' }

// Misma validación que el servidor (RegistroClienteSolicitud), para avisar antes de enviar.
function validar(d) {
  if (!d.nombre.trim()) return 'Escribe tu nombre.'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(d.correo.trim())) return 'Escribe un correo válido.'
  if (!/^[0-9+\- ]{8,20}$/.test(d.telefono.trim())) return 'El teléfono debe tener entre 8 y 20 dígitos (puede llevar + o -).'
  if (!/^[a-z0-9_.]{4,30}$/.test(d.username.trim())) {
    return 'El usuario debe tener de 4 a 30 caracteres: minúsculas, números, punto o guion bajo.'
  }
  if (d.password.length < 8 || d.password.length > 72) return 'La contraseña debe tener entre 8 y 72 caracteres.'
  if (d.password !== d.confirmar) return 'Las contraseñas no coinciden.'
  return null
}

export default function Registro() {
  const { sesion, registrarCliente } = useAuth()
  const navegar = useNavigate()
  const [datos, setDatos] = useState(VACIO)
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  if (sesion) return <Navigate to="/" replace />

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
      await registrarCliente({
        nombre: datos.nombre.trim(),
        correo: datos.correo.trim(),
        telefono: datos.telefono.trim(),
        username: datos.username.trim(),
        password: datos.password,
      })
      navegar('/', { replace: true })
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
        <p className="etiqueta-seccion">Únete al rescate</p>
        <h1>
          Crea tu cuenta y rescata algo <span>delicioso</span>.
        </h1>
        <p className="sub">Reserva paquetes sorpresa, ahorra dinero y suma impacto contra el desperdicio.</p>
      </section>

      <form className="tarjeta" onSubmit={enviar} noValidate>
        <h2>Crear cuenta</h2>
        <p className="sub">Es gratis y toma un minuto.</p>

        {error && (
          <p className="alerta error" role="alert">
            {error}
          </p>
        )}

        <label htmlFor="nombre">Nombre completo</label>
        <input id="nombre" value={datos.nombre} onChange={cambiar('nombre')} autoComplete="name" maxLength={100} required />

        <label htmlFor="correo">Correo</label>
        <input
          id="correo"
          type="email"
          value={datos.correo}
          onChange={cambiar('correo')}
          autoComplete="email"
          maxLength={120}
          required
        />

        <label htmlFor="telefono">Teléfono</label>
        <input
          id="telefono"
          type="tel"
          value={datos.telefono}
          onChange={cambiar('telefono')}
          autoComplete="tel"
          placeholder="8888-8888"
          required
        />

        <label htmlFor="usuario-nuevo">Usuario</label>
        <input
          id="usuario-nuevo"
          value={datos.username}
          onChange={cambiar('username')}
          autoComplete="username"
          autoCapitalize="none"
          placeholder="ej. maria.rojas"
          required
        />

        <label htmlFor="clave-nueva">Contraseña</label>
        <CampoClave
          id="clave-nueva"
          value={datos.password}
          onChange={cambiar('password')}
          autoComplete="new-password"
          placeholder="Mínimo 8 caracteres"
        />

        <label htmlFor="clave-confirmar">Repite la contraseña</label>
        <CampoClave
          id="clave-confirmar"
          value={datos.confirmar}
          onChange={cambiar('confirmar')}
          autoComplete="new-password"
        />

        <button type="submit" className="primario" disabled={enviando}>
          {enviando ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>

        <p className="pie-formulario">
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
        </p>
      </form>
    </main>
  )
}