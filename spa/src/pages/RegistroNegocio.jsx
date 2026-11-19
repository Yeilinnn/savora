import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { api } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import CampoClave from '../components/CampoClave.jsx'
import { Logo } from '../components/Iconos.jsx'

const TIPOS = ['Panadería', 'Soda', 'Restaurante', 'Cafetería', 'Supermercado', 'Otro']
const VACIO = {
  nombre: '',
  tipoNegocio: '',
  ubicacion: '',
  horarioCierre: '19:00',
  username: '',
  password: '',
  confirmar: '',
}

// Misma validación que el servidor (RegistroNegocioSolicitud), para avisar antes de enviar.
function validar(d) {
  if (!d.nombre.trim()) return 'Escribe el nombre del negocio.'
  if (!d.tipoNegocio) return 'Elige el tipo de negocio.'
  if (!d.ubicacion.trim()) return 'Escribe la ubicación del negocio.'
  if (!d.horarioCierre) return 'Indica la hora de cierre.'
  if (!/^[a-z0-9_.]{4,30}$/.test(d.username.trim())) {
    return 'El usuario debe tener de 4 a 30 caracteres: minúsculas, números, punto o guion bajo.'
  }
  if (d.password.length < 8 || d.password.length > 72) return 'La contraseña debe tener entre 8 y 72 caracteres.'
  if (d.password !== d.confirmar) return 'Las contraseñas no coinciden.'
  return null
}

export default function RegistroNegocio() {
  const { sesion } = useAuth()
  const [datos, setDatos] = useState(VACIO)
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [mensaje, setMensaje] = useState(null)

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
      const respuesta = await api.registrarNegocio({
        nombre: datos.nombre.trim(),
        tipoNegocio: datos.tipoNegocio,
        ubicacion: datos.ubicacion.trim(),
        horarioCierre: datos.horarioCierre,
        username: datos.username.trim(),
        password: datos.password,
      })
      setMensaje(respuesta?.mensaje || 'Registro recibido. Un administrador debe aprobar su negocio.')
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
        <p className="etiqueta-seccion">Para negocios</p>
        <h1>
          Dele una segunda vida a su <span>excedente</span>.
        </h1>
        <p className="sub">Publique paquetes sorpresa, venda lo que sobra y done lo que no se vende.</p>
      </section>

      <form className="tarjeta" onSubmit={enviar} noValidate>
        <h2>Registrar negocio</h2>

        {mensaje ? (
          <>
            <p className="alerta ok" role="status">
              {mensaje}
            </p>
            <p className="pie-formulario">
              <Link to="/login">Ir a iniciar sesión</Link>
            </p>
          </>
        ) : (
          <>
            <p className="sub">Un administrador revisará su solicitud antes de activar la cuenta.</p>

            {error && (
              <p className="alerta error" role="alert">
                {error}
              </p>
            )}

            <label htmlFor="n-nombre">Nombre del negocio</label>
            <input id="n-nombre" value={datos.nombre} onChange={cambiar('nombre')} maxLength={100} required />

            <label htmlFor="n-tipo">Tipo de negocio</label>
            <select id="n-tipo" value={datos.tipoNegocio} onChange={cambiar('tipoNegocio')} required>
              <option value="">Elija un tipo</option>
              {TIPOS.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>

            <label htmlFor="n-ubicacion">Ubicación</label>
            <input
              id="n-ubicacion"
              value={datos.ubicacion}
              onChange={cambiar('ubicacion')}
              placeholder="ej. Santa Cruz, Guanacaste"
              maxLength={200}
              required
            />

            <label htmlFor="n-cierre">Hora de cierre</label>
            <input id="n-cierre" type="time" value={datos.horarioCierre} onChange={cambiar('horarioCierre')} required />

            <label htmlFor="n-usuario">Usuario</label>
            <input
              id="n-usuario"
              value={datos.username}
              onChange={cambiar('username')}
              autoComplete="username"
              autoCapitalize="none"
              placeholder="ej. panaderia.espiga"
              required
            />

            <label htmlFor="n-clave">Contraseña</label>
            <CampoClave
              id="n-clave"
              value={datos.password}
              onChange={cambiar('password')}
              autoComplete="new-password"
              placeholder="Mínimo 8 caracteres"
            />

            <label htmlFor="n-clave2">Repita la contraseña</label>
            <CampoClave
              id="n-clave2"
              value={datos.confirmar}
              onChange={cambiar('confirmar')}
              autoComplete="new-password"
            />

            <button type="submit" className="primario" disabled={enviando}>
              {enviando ? 'Enviando…' : 'Enviar solicitud'}
            </button>

            <p className="pie-formulario">
              ¿Ya tiene cuenta? <Link to="/login">Inicie sesión</Link>
            </p>
          </>
        )}
      </form>
    </main>
  )
}