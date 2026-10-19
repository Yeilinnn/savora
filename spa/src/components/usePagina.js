import { useCallback, useEffect, useState } from 'react'

// Carga una página de resultados y expone el estado: 'cargando' | 'error' | 'ok'.
export default function usePagina(cargar, tamano) {
  const [numero, setNumero] = useState(0)
  const [estado, setEstado] = useState('cargando')
  const [pagina, setPagina] = useState(null)
  const [error, setError] = useState(null)
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    let vigente = true
    setEstado('cargando')
    cargar(numero, tamano)
      .then((datos) => {
        if (!vigente) return
        setPagina(datos)
        setEstado('ok')
      })
      .catch((e) => {
        if (!vigente) return
        setError(e)
        setEstado('error')
      })
    return () => {
      vigente = false
    }
  }, [cargar, numero, tamano, intento])

  const reintentar = useCallback(() => setIntento((n) => n + 1), [])
  return { estado, pagina, error, irAPagina: setNumero, reintentar }
}
