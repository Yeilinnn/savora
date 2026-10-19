import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, borrarSesion, EVENTO_NO_AUTORIZADO, guardarSesion, leerSesion } from '../api/client.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [sesion, setSesion] = useState(() => leerSesion())
  const navegar = useNavigate()

  const cerrarSesion = useCallback(() => {
    borrarSesion()
    setSesion(null)
  }, [])

  const iniciarSesion = useCallback(async (username, password) => {
    const respuesta = await api.login(username, password)
    if (respuesta.rol !== 'CLIENTE') {
      throw new Error('Esta aplicación es para clientes. Los negocios usan el panel administrativo.')
    }
    const nueva = { token: respuesta.token, rol: respuesta.rol, username }
    guardarSesion(nueva)
    setSesion(nueva)
  }, [])

  // El módulo cliente avisa con este evento cuando la API responde 401.
  useEffect(() => {
    const alNoAutorizado = () => {
      setSesion(null)
      navegar('/login', { replace: true, state: { vencida: true } })
    }
    window.addEventListener(EVENTO_NO_AUTORIZADO, alNoAutorizado)
    return () => window.removeEventListener(EVENTO_NO_AUTORIZADO, alNoAutorizado)
  }, [navegar])

  const valor = useMemo(() => ({ sesion, iniciarSesion, cerrarSesion }), [sesion, iniciarSesion, cerrarSesion])
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
