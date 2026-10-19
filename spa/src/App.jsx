import { NavLink, Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthContext.jsx'
import Login from './pages/Login.jsx'
import MisReservas from './pages/MisReservas.jsx'
import Paquetes from './pages/Paquetes.jsx'

function RutaProtegida({ children }) {
  const { sesion } = useAuth()
  return sesion ? children : <Navigate to="/login" replace />
}

function Diseno({ children }) {
  const { sesion, cerrarSesion } = useAuth()
  return (
    <>
      <header className="barra">
        <span className="marca">
          Savora <span>rescata comida</span>
        </span>
        <nav>
          <NavLink to="/" end>
            Paquetes
          </NavLink>
          <NavLink to="/reservas">Mis reservas</NavLink>
        </nav>
        <div className="usuario">
          <span>{sesion.username}</span>
          <button type="button" onClick={cerrarSesion}>
            Salir
          </button>
        </div>
      </header>
      <main className="contenido">{children}</main>
    </>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <RutaProtegida>
            <Diseno>
              <Paquetes />
            </Diseno>
          </RutaProtegida>
        }
      />
      <Route
        path="/reservas"
        element={
          <RutaProtegida>
            <Diseno>
              <MisReservas />
            </Diseno>
          </RutaProtegida>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
