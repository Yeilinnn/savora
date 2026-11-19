import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { useAuth } from '../auth/AuthContext.jsx'

// Carrito de compras del cliente: guarda los paquetes elegidos hasta que decide pagar.
// Vive en sessionStorage (se pierde al cerrar la pestaña) y se vacía al cerrar sesión.

const Contexto = createContext(null)
export const MAX_CARRITO = 5
const CLAVE = 'savora.carrito'

function leer() {
  try {
    const crudo = sessionStorage.getItem(CLAVE)
    const lista = crudo ? JSON.parse(crudo) : []
    return Array.isArray(lista) ? lista : []
  } catch {
    return []
  }
}

function guardar(items) {
  try {
    sessionStorage.setItem(CLAVE, JSON.stringify(items))
  } catch {
    /* sin almacenamiento: el carrito vive solo en memoria */
  }
}

export function CarritoProvider({ children }) {
  const { sesion } = useAuth()
  const [items, setItems] = useState(leer)
  const [abierto, setAbierto] = useState(false)

  useEffect(() => {
    guardar(items)
  }, [items])

  useEffect(() => {
    if (!sesion) {
      setItems([])
      setAbierto(false)
    }
  }, [sesion])

  const agregar = useCallback((paquete) => {
    setItems((actuales) => {
      if (actuales.length >= MAX_CARRITO || actuales.some((i) => i.id === paquete.id)) return actuales
      return [
        ...actuales,
        {
          id: paquete.id,
          descripcion: paquete.descripcion,
          nombreNegocio: paquete.nombreNegocio,
          precioConDescuento: paquete.precioConDescuento,
          precioOriginal: paquete.precioOriginal,
        },
      ]
    })
  }, [])

  const quitar = useCallback((id) => setItems((actuales) => actuales.filter((i) => i.id !== id)), [])
  const vaciar = useCallback(() => setItems([]), [])
  const abrir = useCallback(() => setAbierto(true), [])
  const cerrar = useCallback(() => setAbierto(false), [])

  const valor = useMemo(
    () => ({
      items,
      total: items.reduce((suma, i) => suma + Number(i.precioConDescuento), 0),
      lleno: items.length >= MAX_CARRITO,
      contiene: (id) => items.some((i) => i.id === id),
      agregar,
      quitar,
      vaciar,
      abierto,
      abrir,
      cerrar,
    }),
    [items, agregar, quitar, vaciar, abierto, abrir, cerrar],
  )
  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>
}

export function useCarrito() {
  return useContext(Contexto)
}