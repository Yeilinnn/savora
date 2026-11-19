import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  api,
  ApiError,
  borrarSesion,
  clienteIdDeToken,
  EVENTO_NO_AUTORIZADO,
  guardarSesion,
  leerSesion,
  negocioIdDeToken,
} from '../api/client.js'

const AuthContext = createContext(null)

// Sesiones guardadas por versiones anteriores no traían clienteId/negocioId: se completan desde el token.
function sesionInicial() {
  const guardada = leerSesion()
  if (!guardada) return null
  return {
    ...guardada,
    clienteId: guardada.clienteId ?? clienteIdDeToken(guardada.token),
    negocioId: guardada.negocioId ?? negocioIdDeToken(guardada.token),
  }
}

function construirSesion(respuesta, username) {
  return {
    token: respuesta.token,
    rol: respuesta.rol,
    username,
    clienteId: clienteIdDeToken(respuesta.token),
    negocioId: negocioIdDeToken(respuesta.token),
  }
}

export function AuthProvider({ children }) {
  const [sesion, setSesion] = useState(sesionInicial)
  const navegar = useNavigate()

  const cerrarSesion = useCallback(() => {
    borrarSesion()
    setSesion(null)
  }, [])

  const abrirSesion = useCallback((respuesta, username) => {
    const nueva = construirSesion(respuesta, username)
    guardarSesion(nueva)
    setSesion(nueva)
  }, [])

  // Sirve para clientes y para negocios: la app muestra una vista distinta según el rol.
  const iniciarSesion = useCallback(
    async (username, password) => {
      const respuesta = await api.login(username, password)
      // Los administradores usan el panel web de la API, no esta aplicación.
      if (respuesta.rol === 'ADMIN') {
        throw new ApiError(403, { detail: 'Las cuentas de administrador entran por el panel administrativo de la API.' })
      }
      abrirSesion(respuesta, username)
    },
    [abrirSesion],
  )

  // Registro de un cliente nuevo: la API devuelve un token, así que queda con la sesión abierta.
  const registrarCliente = useCallback(
    async (datos) => {
      abrirSesion(await api.registrar(datos), datos.username)
    },
    [abrirSesion],
  )

  // El módulo cliente avisa con este evento cuando la API responde 401.
  useEffect(() => {
    const alNoAutorizado = () => {
      setSesion(null)
      navegar('/login', { replace: true, state: { vencida: true } })
    }
    window.addEventListener(EVENTO_NO_AUTORIZADO, alNoAutorizado)
    return () => window.removeEventListener(EVENTO_NO_AUTORIZADO, alNoAutorizado)
  }, [navegar])

  const valor = useMemo(
    () => ({ sesion, iniciarSesion, registrarCliente, cerrarSesion }),
    [sesion, iniciarSesion, registrarCliente, cerrarSesion],
  )
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}