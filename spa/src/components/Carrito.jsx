import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api, EVENTO_RESERVAS_CAMBIARON } from '../api/client.js'
import { useCarrito } from '../carrito/CarritoContext.jsx'
import { formatearMoneda } from './Formato.js'
import PagoSimulado from './PagoSimulado.jsx'
import './pago.css'

function mensajeDeError(e) {
  if (e.estado === 404) return 'Uno de los paquetes ya no existe. Quítelo del carrito e intente de nuevo.'
  if (e.estado === 403) return 'Su cuenta no puede realizar reservas.'
  return e.message
}

// Ventana del carrito: lista -> pago -> comprobante.
export default function Carrito() {
  const { items, total, quitar, vaciar, abierto, cerrar } = useCarrito()
  const [paso, setPaso] = useState('lista')
  const [comprobante, setComprobante] = useState(null)

  if (!abierto) return null

  function cerrarVentana() {
    setPaso('lista')
    setComprobante(null)
    cerrar()
  }

  // Lo llama la pantalla de pago. Si la API rechaza algo, lanza el error y la pantalla lo muestra.
  async function pagar(tarjeta) {
    try {
      const respuesta = await api.pagarCarrito(
        items.map((i) => i.id),
        tarjeta,
      )
      setComprobante(respuesta)
      vaciar()
      setPaso('listo')
      window.dispatchEvent(new Event(EVENTO_RESERVAS_CAMBIARON))
    } catch (e) {
      throw new Error(mensajeDeError(e))
    }
  }

  if (paso === 'pago' && items.length > 0) {
    return <PagoSimulado lineas={items} total={total} alCancelar={() => setPaso('lista')} alConfirmar={pagar} />
  }

  if (paso === 'listo' && comprobante) {
    return (
      <div className="pago-fondo" role="dialog" aria-modal="true" aria-labelledby="carrito-titulo">
        <div className="pago-caja pago-exito">
          <div className="pago-sello" aria-hidden="true">
            ✓
          </div>
          <h2 id="carrito-titulo">¡Pago aprobado!</h2>
          <p className="sub">
            {comprobante.marca} ···· {comprobante.ultimos4} · ref. {comprobante.referencia}
          </p>
          <div className="pago-resumen">
            {comprobante.reservas.map((r) => (
              <div key={r.id} className="pago-linea">
                <span>{r.descripcionPaquete}</span>
                <span className="precio">{formatearMoneda(r.precioConDescuento)}</span>
              </div>
            ))}
            <div className="pago-linea pago-total">
              <span>Total pagado</span>
              <strong>{formatearMoneda(comprobante.total)}</strong>
            </div>
          </div>
          <p className="sub" style={{ marginTop: 12 }}>
            Sus reservas quedaron listas para recoger.
          </p>
          <div className="pago-acciones">
            <button type="button" onClick={cerrarVentana}>
              Seguir explorando
            </button>
            <Link to="/reservas" className="enlace-boton" onClick={cerrarVentana}>
              Ver mis reservas
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="pago-fondo" role="dialog" aria-modal="true" aria-labelledby="carrito-titulo">
      <div className="pago-caja">
        <h2 id="carrito-titulo">Su carrito</h2>
        {items.length === 0 ? (
          <p className="pago-vacio">Todavía no ha agregado paquetes. ¡Explore y rescate algo rico!</p>
        ) : (
          <div className="pago-resumen">
            {items.map((i) => (
              <div key={i.id} className="pago-linea">
                <span>
                  {i.descripcion}
                  <small>{i.nombreNegocio}</small>
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span className="precio">{formatearMoneda(i.precioConDescuento)}</span>
                  <button type="button" className="pago-quitar" onClick={() => quitar(i.id)}>
                    Quitar
                  </button>
                </span>
              </div>
            ))}
            <div className="pago-linea pago-total">
              <span>Total</span>
              <strong>{formatearMoneda(total)}</strong>
            </div>
          </div>
        )}
        <div className="pago-acciones">
          <button type="button" onClick={cerrarVentana}>
            Seguir explorando
          </button>
          <button type="button" className="primario" disabled={items.length === 0} onClick={() => setPaso('pago')}>
            Continuar al pago
          </button>
        </div>
      </div>
    </div>
  )
}