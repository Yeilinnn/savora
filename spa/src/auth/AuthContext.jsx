import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  api,
  borrarSesion,
  clienteIdDeToken,
  EVENTO_NO_AUTORIZADO,
  guardarSesion,
  leerSesion,
} from '../api/client.js'

const AuthContext = createContext(null)

// Sesiones guardadas por versiones anteriores no traían clienteId: se completa desde el token.
function sesionInicial() {
  const guardada = leerSesion()
  if (guardada && (guardada.clienteId === undefined || guardada.clienteId === null)) {
    return { ...guardada, clienteId: clienteIdDeToken(guardada.token) }
  }
  return guardada
}

export function AuthProvider({ children }) {
  const [sesion, setSesion] = useState(sesionInicial)
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
    const nueva = {
      token: respuesta.token,
      rol: respuesta.rol,
      username,
      clienteId: clienteIdDeToken(respuesta.token),
    }
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