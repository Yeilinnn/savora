const moneda = new Intl.NumberFormat('es-CR', { style: 'currency', currency: 'CRC', maximumFractionDigits: 0 })
const fechaHora = new Intl.DateTimeFormat('es-CR', { dateStyle: 'short', timeStyle: 'short' })
const soloHora = new Intl.DateTimeFormat('es-CR', { hour: 'numeric', minute: '2-digit' })
const fechaCorta = new Intl.DateTimeFormat('es-CR', { day: 'numeric', month: 'short' })

export const formatearMoneda = (valor) => moneda.format(Number(valor))
export const formatearFechaHora = (valor) => fechaHora.format(new Date(valor))

// "hoy, 7:30 p. m." / "mañana, 9:00 a. m." / "12 nov, 7:30 p. m."
export function formatearLimite(valor) {
  const fecha = new Date(valor)
  const hoy = new Date()
  const manana = new Date(hoy)
  manana.setDate(hoy.getDate() + 1)
  const hora = soloHora.format(fecha)
  if (fecha.toDateString() === hoy.toDateString()) return `hoy, ${hora}`
  if (fecha.toDateString() === manana.toDateString()) return `mañana, ${hora}`
  return `${fechaCorta.format(fecha)}, ${hora}`
}

// Porcentaje de descuento entero: precio 7000 -> 3000 = 57.
export function porcentajeDescuento(precioOriginal, precioConDescuento) {
  const original = Number(precioOriginal)
  const final = Number(precioConDescuento)
  if (!(original > 0)) return 0
  return Math.max(0, Math.round((1 - final / original) * 100))
}

// Colones que se ahorró el cliente en una reserva (a partir del precio pagado y el % de descuento).
export function ahorroDeReserva(reserva) {
  const precio = Number(reserva.precioConDescuento)
  const descuento = Number(reserva.descuentoPorcentaje)
  if (!(descuento > 0 && descuento < 100)) return 0
  return (precio * descuento) / (100 - descuento)
}

export const formatearKg = (kg) =>
  `${Number(kg).toLocaleString('es-CR', { minimumFractionDigits: 0, maximumFractionDigits: 1 })} kg`