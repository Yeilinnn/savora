import { useState } from 'react'
import { api } from '../api/client.js'
import CampoClave from './CampoClave.jsx'

// Formulario de cambio de contraseña con la sesión abierta (cliente y negocio).
export default function CambiarContrasena() {
  const [abierto, setAbierto] = useState(false)
  const [actual, setActual] = useState('')
  const [nueva, setNueva] = useState('')
  const [confirmar, setConfirmar] = useState('')
  const [error, setError] = useState(null)
  const [listo, setListo] = useState(false)
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento) {
    evento.preventDefault()
    setListo(false)
    if (nueva.length < 8 || nueva.length > 72) {
      setError('La nueva contraseña debe tener entre 8 y 72 caracteres.')
      return
    }
    if (nueva !== confirmar) {
      setError('Las contraseñas nuevas no coinciden.')
      return
    }
    setError(null)
    setEnviando(true)
    try {
      await api.cambiarContrasena(actual, nueva)
      setActual('')
      setNueva('')
      setConfirmar('')
      setListo(true)
      setAbierto(false)
    } catch (e) {
      setError(e.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="cambiar-clave">
      {listo && (
        <p className="alerta ok" role="status">
          Contraseña actualizada.
        </p>
      )}
      {!abierto ? (
        <button type="button" className="enlace-boton secundario ancho" onClick={() => setAbierto(true)}>
          Cambiar contraseña
        </button>
      ) : (
        <form className="tarjeta" onSubmit={enviar}>
          <h2>Cambiar contraseña</h2>
          {error && (
            <p className="alerta error" role="alert">
              {error}
            </p>
          )}
          <label htmlFor="clave-actual">Contraseña actual</label>
          <CampoClave
            id="clave-actual"
            value={actual}
            onChange={(e) => setActual(e.target.value)}
            autoComplete="current-password"
          />
          <label htmlFor="clave-nueva-cambio">Nueva contraseña</label>
          <CampoClave
            id="clave-nueva-cambio"
            value={nueva}
            onChange={(e) => setNueva(e.target.value)}
            autoComplete="new-password"
            placeholder="Mínimo 8 caracteres"
          />
          <label htmlFor="clave-confirmar-cambio">Repite la nueva contraseña</label>
          <CampoClave
            id="clave-confirmar-cambio"
            value={confirmar}
            onChange={(e) => setConfirmar(e.target.value)}
            autoComplete="new-password"
          />
          <button type="submit" className="primario" disabled={enviando}>
            {enviando ? 'Guardando…' : 'Guardar contraseña'}
          </button>
          <button
            type="button"
            className="enlace-boton secundario ancho"
            onClick={() => {
              setAbierto(false)
              setError(null)
            }}
          >
            Cancelar
          </button>
        </form>
      )}
    </div>
  )
}