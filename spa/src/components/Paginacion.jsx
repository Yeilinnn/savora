export default function Paginacion({ pagina, alCambiar }) {
  if (pagina.totalPaginas <= 1) return null
  return (
    <nav className="paginacion" aria-label="Paginación">
      <button type="button" onClick={() => alCambiar(pagina.numero - 1)} disabled={pagina.numero === 0}>
        « Anterior
      </button>
      <span>
        Página <b>{pagina.numero + 1}</b> de <b>{pagina.totalPaginas}</b> ({pagina.totalElementos} resultados)
      </span>
      <button
        type="button"
        onClick={() => alCambiar(pagina.numero + 1)}
        disabled={pagina.numero + 1 >= pagina.totalPaginas}
      >
        Siguiente »
      </button>
    </nav>
  )
}
