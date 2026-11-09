// Catálogo de insignias para clientes. Los códigos coinciden con CodigoInsignia del backend.
// Hoy el backend solo otorga PRIMER_RESCATE y GUARDIAN_DEL_BARRIO a clientes; la racha de
// 3 semanas está definida pero todavía no se otorga (se muestra como "Próximamente").
export const INSIGNIAS = [
  {
    codigo: 'PRIMER_RESCATE',
    nombre: 'Primer rescate',
    descripcion: 'Recogiste tu primer paquete sorpresa.',
    emoji: '🌱',
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
    codigo: 'RACHA_3_SEMANAS',
    nombre: 'Racha de 3 semanas',
    descripcion: 'Rescata comida 3 semanas seguidas.',
    emoji: '🔥',
    disponible: false,
  },
]