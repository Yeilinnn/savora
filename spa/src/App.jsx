import { useCallback, useEffect, useState } from 'react'
import { Link, NavLink, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { api, EVENTO_RESERVAS_CAMBIARON } from './api/client.js'
import { useAuth } from './auth/AuthContext.jsx'
import { useCarrito } from './carrito/CarritoContext.jsx'
import Carrito from './components/Carrito.jsx'
import Icono, { Logo } from './components/Iconos.jsx'
import Impacto from './pages/Impacto.jsx'
import Login from './pages/Login.jsx'
import MisReservas from './pages/MisReservas.jsx'
import NegocioCuenta from './pages/NegocioCuenta.jsx'
import NegocioImpacto from './pages/NegocioImpacto.jsx'
import NegocioPaquetes from './pages/NegocioPaquetes.jsx'
import NegocioReservas from './pages/NegocioReservas.jsx'
import OlvideContrasena from './pages/OlvideContrasena.jsx'
import Paquetes from './pages/Paquetes.jsx'
import Perfil from './pages/Perfil.jsx'
import Registro from './pages/Registro.jsx'
import RegistroNegocio from './pages/RegistroNegocio.jsx'
import RestablecerContrasena from './pages/RestablecerContrasena.jsx'

const ES_NEGOCIO = (sesion) => sesion?.rol === 'NEGOCIO'

function RutaProtegida({ children }) {
  const { sesion } = useAuth()
  return sesion ? children : <Navigate to="/login" replace />
}

function Diseno({ children }) {
  const { sesion } = useAuth()
  const ubicacion = useLocation()
  const [pendientes, setPendientes] = useState(0)
  const negocio = ES_NEGOCIO(sesion)
  const { items, abrir } = useCarrito()

  const refrescar = useCallback(() => {
    // Cliente: sus reservas pendientes. Negocio: reservas recibidas por entregar.
    const contar = negocio
      ? api.listarReservasNegocio(0, 1, 'pendiente').then((p) => p.totalElementos)
      : api.contarReservasPendientes()
    contar.then(setPendientes).catch(() => {})
  }, [negocio])

  // El contador se actualiza al navegar y cuando cambian las reservas.
  useEffect(() => {
    refrescar()
  }, [refrescar, ubicacion.pathname])

  useEffect(() => {
    window.addEventListener(EVENTO_RESERVAS_CAMBIARON, refrescar)
    return () => window.removeEventListener(EVENTO_RESERVAS_CAMBIARON, refrescar)
  }, [refrescar])

  const enlaces = negocio
    ? [
        { a: '/', texto: 'Paquetes', icono: 'caja', fin: true },
        { a: '/reservas', texto: 'Reservas', icono: 'bolsa', contador: pendientes },
        { a: '/impacto', texto: 'Impacto', icono: 'hoja' },
        { a: '/perfil', texto: 'Cuenta', icono: 'usuario' },
      ]
    : [
        { a: '/', texto: 'Explorar', icono: 'buscar', fin: true },
        { a: '/reservas', texto: 'Reservas', icono: 'bolsa', contador: pendientes },
        { a: '/impacto', texto: 'Impacto', icono: 'hoja' },
        { a: '/perfil', texto: 'Perfil', icono: 'usuario' },
      ]

  return (
    <>
      <header className="barra">
        <Link to="/" className="marca" aria-label="Savora, ir al inicio">
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
        <div className="barra-acciones">
          {!negocio && (
            <button
              type="button"
              className="carrito-boton"
              onClick={abrir}
              aria-label={`Abrir carrito, ${items.length} paquetes`}
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="9" cy="20" r="1.4" />
                <circle cx="18" cy="20" r="1.4" />
                <path d="M3 4h2.2l2.2 11h10.2l2-8H6.4" />
              </svg>
              {items.length > 0 && <span className="contador">{items.length}</span>}
            </button>
          )}
          <Link to="/perfil" className="avatar" aria-label={negocio ? 'Mi negocio' : 'Mi perfil'}>
            {sesion.username.charAt(0).toUpperCase()}
          </Link>
        </div>
      </header>
      <main className="contenido">{children}</main>
      {!negocio && <Carrito />}
    </>
  )
}

// Cada ruta muestra la pantalla de cliente o la de negocio según el rol de la sesión.
function Pantalla({ cliente, negocio }) {
  const { sesion } = useAuth()
  return (
    <RutaProtegida>
      <Diseno>{ES_NEGOCIO(sesion) ? negocio : cliente}</Diseno>
    </RutaProtegida>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/registro" element={<Registro />} />
      <Route path="/registro-negocio" element={<RegistroNegocio />} />
      <Route path="/olvide" element={<OlvideContrasena />} />
      <Route path="/restablecer" element={<RestablecerContrasena />} />
      <Route path="/" element={<Pantalla cliente={<Paquetes />} negocio={<NegocioPaquetes />} />} />
      <Route path="/reservas" element={<Pantalla cliente={<MisReservas />} negocio={<NegocioReservas />} />} />
      <Route path="/impacto" element={<Pantalla cliente={<Impacto />} negocio={<NegocioImpacto />} />} />
      <Route path="/perfil" element={<Pantalla cliente={<Perfil />} negocio={<NegocioCuenta />} />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}