// Muestra los tres estados de una consulta: cargando, error y (si hay datos) el contenido.
export default function EstadoCarga({ estado, error, alReintentar, children }) {
  if (estado === 'cargando') {
    return (
      <p className="estado cargando" role="status">
        Cargando…
      </p>
    )
  }
  if (estado === 'error') {
    return (
      <div className="estado error" role="alert">
        <p>{error?.message || 'Ocurrió un error inesperado.'}</p>
        {alReintentar && (
          <button type="button" onClick={alReintentar}>
            Reintentar
          </button>
        )}
      </div>
    )
  }
  return children
}
