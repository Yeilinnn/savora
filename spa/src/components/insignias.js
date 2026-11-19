// Catálogo de insignias. Los códigos coinciden con CodigoInsignia del backend.

export const INSIGNIAS = [
  {
    codigo: 'PRIMER_RESCATE',
    nombre: 'Primer rescate',
    descripcion: 'Recogiste tu primer paquete sorpresa.',
    emoji: '🌱',
    disponible: true,
  },
  {
    codigo: 'RACHA_3_SEMANAS',
    nombre: 'Racha de 3 semanas',
    descripcion: 'Recogiste paquetes 3 semanas seguidas.',
    emoji: '🔥',
    disponible: true,
  },
  {
    codigo: 'GUARDIAN_DEL_BARRIO',
    nombre: 'Guardián del barrio',
    descripcion: 'Recogiste 10 paquetes.',
    emoji: '🛡️',
    disponible: true,
  },
  {
    codigo: 'HEROE_DEL_PLANETA',
    nombre: 'Héroe del planeta',
    descripcion: 'Recogiste 25 paquetes.',
    emoji: '🌎',
    disponible: true,
  },
]

export const INSIGNIAS_NEGOCIO = [
  {
    codigo: 'PRIMERA_VENTA',
    nombre: 'Primera venta',
    descripcion: 'Un cliente recogió tu primer paquete sorpresa.',
    emoji: '🛍️',
  },
  {
    codigo: 'NEGOCIO_CONSTANTE',
    nombre: 'Negocio constante',
    descripcion: 'Clientes recogieron tus paquetes 3 semanas seguidas.',
    emoji: '🔥',
  },
  {
    codigo: 'PRIMERA_DONACION',
    nombre: 'Primera donación',
    descripcion: 'Donaste un paquete a una organización comunitaria.',
    emoji: '🤝',
  },
  {
    codigo: 'CERO_DESPERDICIO',
    nombre: 'Cero desperdicio',
    descripcion: 'Al menos 10 paquetes cerrados y 80 % o más donados en lugar de perdidos.',
    emoji: '♻️',
  },
]

// Texto de la racha semanal (clientes y negocios).
export function textoRacha(actual, mejor, esNegocio) {
  const semanas = (n) => (n === 1 ? '1 semana' : `${n} semanas`)
  if (actual > 0) {
    return `${esNegocio ? 'Tu negocio lleva' : 'Llevas'} ${semanas(actual)} seguida${actual === 1 ? '' : 's'}. Tu mejor racha: ${semanas(mejor)}.`
  }
  return mejor > 0
    ? `Tu racha se reinició. Tu mejor racha fue de ${semanas(mejor)}. ${esNegocio ? 'Una recogida esta semana la reactiva.' : 'Recoge un paquete esta semana para reactivarla.'}`
    : esNegocio
      ? 'Cuando un cliente recoja un paquete tuyo empieza la racha.'
      : 'Recoge un paquete esta semana para empezar tu racha.'
}