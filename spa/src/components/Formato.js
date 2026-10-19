const moneda = new Intl.NumberFormat('es-CR', { style: 'currency', currency: 'CRC', maximumFractionDigits: 0 })
const fechaHora = new Intl.DateTimeFormat('es-CR', { dateStyle: 'short', timeStyle: 'short' })

export const formatearMoneda = (valor) => moneda.format(Number(valor))
export const formatearFechaHora = (valor) => fechaHora.format(new Date(valor))
