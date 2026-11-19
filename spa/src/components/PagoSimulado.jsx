import { useState } from 'react'
import { formatearMoneda } from './Formato.js'
import './pago.css'

// Pantalla de pago SIMULADO: no se cobra nada ni se envía la tarjeta a ningún banco real.
// La API valida los datos, aprueba o rechaza según la tarjeta de prueba y guarda solo la marca
// y los últimos 4 dígitos.

function formatearNumero(valor) {
  return valor
    .replace(/\D/g, '')
    .slice(0, 19)
    .replace(/(.{4})/g, '$1 ')
    .trim()
}

function formatearVencimiento(valor) {
  const digitos = valor.replace(/\D/g, '').slice(0, 4)
  return digitos.length > 2 ? `${digitos.slice(0, 2)}/${digitos.slice(2)}` : digitos
}

function validar(d) {
  if (!d.titular.trim()) return 'Escriba el nombre que aparece en la tarjeta.'
  const numero = d.numero.replace(/\s/g, '')
  if (numero.length < 13 || numero.length > 19) return 'El número de tarjeta debe tener entre 13 y 19 dígitos.'
  if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(d.vencimiento)) return 'El vencimiento debe tener el formato MM/AA.'
  if (!/^\d{3,4}$/.test(d.cvv)) return 'El código de seguridad (CVV) debe tener 3 o 4 dígitos.'
  return null
}

// lineas = [{ id, descripcion, precioConDescuento }], total = suma en colones.
export default function PagoSimulado({ lineas, total, alCancelar, alConfirmar }) {
  const [datos, setDatos] = useState({ titular: '', numero: '', vencimiento: '', cvv: '' })
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

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
      await alConfirmar({
        titular: datos.titular.trim(),
        numero: datos.numero,
        vencimiento: datos.vencimiento,
        cvv: datos.cvv,
      })
    } catch (e) {
      setError(e.message)
      setEnviando(false)
    }
  }

  return (
    <div className="pago-fondo" role="dialog" aria-modal="true" aria-labelledby="pago-titulo">
      <form className="pago-caja" onSubmit={enviar} noValidate>
        <h2 id="pago-titulo">Pagar y reservar</h2>
        <div className="pago-resumen">
          {lineas.map((l) => (
            <div key={l.id} className="pago-linea">
              <span>{l.descripcion}</span>
              <span className="precio">{formatearMoneda(l.precioConDescuento)}</span>
            </div>
          ))}
          <div className="pago-linea pago-total">
            <span>Total</span>
            <strong>{formatearMoneda(total)}</strong>
          </div>
        </div>
        <p className="pago-aviso">
          Pago de <strong>simulación</strong>: no se cobra nada. Tarjeta de prueba: <strong>4242 4242 4242 4242</strong>,
          cualquier fecha futura y CVV 123. La <strong>4000 0000 0000 0002</strong> simula un rechazo.
        </p>

        {error && (
          <p className="alerta error" role="alert" style={{ marginTop: 12 }}>
            {error}
          </p>
        )}

        <label htmlFor="pago-titular">Nombre en la tarjeta</label>
        <input
          id="pago-titular"
          value={datos.titular}
          onChange={(e) => setDatos({ ...datos, titular: e.target.value })}
          autoComplete="cc-name"
          maxLength={60}
        />

        <label htmlFor="pago-numero">Número de tarjeta</label>
        <input
          id="pago-numero"
          value={datos.numero}
          onChange={(e) => setDatos({ ...datos, numero: formatearNumero(e.target.value) })}
          inputMode="numeric"
          autoComplete="cc-number"
          placeholder="4242 4242 4242 4242"
        />

        <div className="pago-fila">
          <div>
            <label htmlFor="pago-venc">Vencimiento</label>
            <input
              id="pago-venc"
              value={datos.vencimiento}
              onChange={(e) => setDatos({ ...datos, vencimiento: formatearVencimiento(e.target.value) })}
              inputMode="numeric"
              autoComplete="cc-exp"
              placeholder="MM/AA"
            />
          </div>
          <div>
            <label htmlFor="pago-cvv">CVV</label>
            <input
              id="pago-cvv"
              value={datos.cvv}
              onChange={(e) => setDatos({ ...datos, cvv: e.target.value.replace(/\D/g, '').slice(0, 4) })}
              inputMode="numeric"
              autoComplete="cc-csc"
              placeholder="123"
            />
          </div>
        </div>

        <div className="pago-acciones">
          <button type="button" onClick={alCancelar} disabled={enviando}>
            Volver
          </button>
          <button type="submit" className="primario" disabled={enviando}>
            {enviando ? 'Procesando…' : `Pagar ${formatearMoneda(total)}`}
          </button>
        </div>
      </form>
    </div>
  )
}