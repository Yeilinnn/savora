import { useState } from 'react'
import { api } from '../api/client.js'
import './resenas.css'

// Calificación (1 a 5 estrellas) y comentario opcional de una reserva ya recogida.
export default function FormularioResena({ reservaId, alEnviar, alCancelar }) {
  const [calificacion, setCalificacion] = useState(0)
  const [comentario, setComentario] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState(null)

  async function enviar(evento) {
    evento.preventDefault()
    if (calificacion < 1) {
      setError('Elija de 1 a 5 estrellas.')
      return
    }
    setEnviando(true)
    setError(null)
    try {
      await api.calificarReserva(reservaId, calificacion, comentario.trim() || null)
      alEnviar(reservaId)
    } catch (e) {
      // 409: ya estaba calificada; se trata igual que un éxito para ocultar el botón.
      if (e?.estado === 409) {
        alEnviar(reservaId)
        return
      }
      setError(e?.message || 'No se pudo guardar la reseña. Intente de nuevo.')
      setEnviando(false)
    }
  }

  return (
    <form className="form-resena" onSubmit={enviar}>
      <div className="selector-estrellas" role="radiogroup" aria-label="Calificación">
        {[1, 2, 3, 4, 5].map((n) => (
          <button
            key={n}
            type="button"
            className={n <= calificacion ? 'activa' : ''}
            onClick={() => setCalificacion(n)}
            role="radio"
            aria-checked={n === calificacion}
            aria-label={`${n} ${n === 1 ? 'estrella' : 'estrellas'}`}
          >
            ★
          </button>
        ))}
      </div>
      <textarea
        value={comentario}
        onChange={(e) => setComentario(e.target.value)}
        maxLength={500}
        placeholder="¿Cómo estuvo el paquete? (opcional)"
        aria-label="Comentario"
      />
      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}
      <div className="acciones">
        <button type="submit" className="primario" disabled={enviando}>
          {enviando ? 'Enviando…' : 'Enviar reseña'}
        </button>
        <button type="button" onClick={alCancelar} disabled={enviando}>
          Cancelar
        </button>
      </div>
    </form>
  )
}