// Ilustración (emoji + color de fondo) según la categoría o la descripción del paquete.
// Las clases "cat-*" están definidas en styles.css.

function normalizar(texto) {
  return (texto || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
}

const REGLAS = [
  { palabras: ['panader', 'pan ', 'pan,', 'baguette', 'pastel', 'reposter'], emoji: '🥖', clase: 'cat-pan' },
  { palabras: ['fruta', 'verdura', 'vegetal', 'hortaliza'], emoji: '🍊', clase: 'cat-fruta' },
  { palabras: ['comida', 'combo', 'almuerzo', 'plato', 'sopa', 'casado', 'cena'], emoji: '🍲', clase: 'cat-comida' },
]

export function visualDe(texto) {
  const t = `${normalizar(texto)} `
  const regla = REGLAS.find((r) => r.palabras.some((p) => t.includes(p)))
  return regla ? { emoji: regla.emoji, clase: regla.clase } : { emoji: '🛍️', clase: 'cat-otros' }
}