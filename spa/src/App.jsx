import { useCallback, useEffect, useState } from 'react'
import { Link, NavLink, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { api, EVENTO_RESERVAS_CAMBIARON } from './api/client.js'
import { useAuth } from './auth/AuthContext.jsx'
import Icono, { Logo } from './components/Iconos.jsx'
import Impacto from './pages/Impacto.jsx'
import Login from './pages/Login.jsx'
import MisReservas from './pages/MisReservas.jsx'
import Paquetes from './pages/Paquetes.jsx'
import Perfil from './pages/Perfil.jsx'

function RutaProtegida({ children }) {
  const { sesion } = useAuth()
  return sesion ? children : <Navigate to="/login" replace />
}

function Diseno({ children }) {
  const { sesion } = useAuth()
  const ubicacion = useLocation()
  const [pendientes, setPendientes] = useState(0)

  const refrescar = useCallback(() => {
    api
      .contarReservasPendientes()
      .then(setPendientes)
      .catch(() => {})
  }, [])

  // El contador se actualiza al navegar y cuando se crea una reserva.
  useEffect(() => {
    refrescar()
  }, [refrescar, ubicacion.pathname])

  useEffect(() => {
    window.addEventListener(EVENTO_RESERVAS_CAMBIARON, refrescar)
    return () => window.removeEventListener(EVENTO_RESERVAS_CAMBIARON, refrescar)
  }, [refrescar])

  const enlaces = [
    { a: '/', texto: 'Explorar', icono: 'buscar', fin: true },
    { a: '/reservas', texto: 'Reservas', icono: 'bolsa', contador: pendientes },
    { a: '/impacto', texto: 'Impacto', icono: 'hoja' },
    { a: '/perfil', texto: 'Perfil', icono: 'usuario' },
  ]

  return (
    <>
      <header className="barra">
        <Link to="/" className="marca" aria-label="Savora, ir a Explorar">
          <Logo />
          <span>Savora</span>
        </Link>
        <nav className="navegacion" aria-label="Principal">
          {enlaces.map((e) => (
            <NavLink key={e.a} to={e.a} end={e.fin} className="enlace-nav">
              <span className="enlace-icono">
                <Icono nombre={e.icono} tamano={22} />
                {e.contador > 0 && (
                  <span className="contador" aria-label={`${e.contador} pendientes`}>
                    {e.contador}
                  </span>
                )}
              </span>
              <span>{e.texto}</span>
            </NavLink>
          ))}
        </nav>
        <Link to="/perfil" className="avatar" aria-label="Mi perfil">
          {sesion.username.charAt(0).toUpperCase()}
        </Link>
      </header>
      <main className="contenido">{children}</main>
    </>
  )
}

function Pantalla({ children }) {
  return (
    <RutaProtegida>
      <Diseno>{children}</Diseno>
    </RutaProtegida>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Pantalla><Paquetes /></Pantalla>} />
      <Route path="/reservas" element={<Pantalla><MisReservas /></Pantalla>} />
      <Route path="/impacto" element={<Pantalla><Impacto /></Pantalla>} />
      <Route path="/perfil" element={<Pantalla><Perfil /></Pantalla>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}