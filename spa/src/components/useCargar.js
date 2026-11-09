import { useCallback, useEffect, useState } from 'react'

// Carga un dato (una promesa) y expone el estado: 'cargando' | 'error' | 'ok'.
// "dependencias" son los valores que, al cambiar, vuelven a lanzar la carga.
export default function useCargar(cargar, dependencias = []) {
  const [estado, setEstado] = useState('cargando')
  const [datos, setDatos] = useState(null)
  const [error, setError] = useState(null)
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    let vigente = true
    setEstado('cargando')
    cargar()
      .then((resultado) => {
        if (!vigente) return
        setDatos(resultado)
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...dependencias, intento])

  const reintentar = useCallback(() => setIntento((n) => n + 1), [])
  return { estado, datos, error, reintentar }
}