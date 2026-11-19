import './resenas.css'

// Muestra una calificación de 0 a 5 como estrellas (acepta decimales: 4.3 se ve como 4 llenas).
export default function Estrellas({ valor }) {
  const llenas = Math.round(Number(valor) || 0)
  return (
    <span className="estrellas" role="img" aria-label={`${Number(valor).toFixed(1)} de 5 estrellas`}>
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} className={n <= llenas ? '' : 'apagada'} aria-hidden="true">
          ★
        </span>
      ))}
    </span>
  )
}