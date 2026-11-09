// Módulo cliente: TODAS las llamadas a la API pasan por aquí.
// Centraliza la URL base, el header Authorization, los errores Problem Details (RFC 9457)
// y el manejo del 401 (token vencido o inválido).

const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')
const CLAVE_SESION = 'savora.sesion'

export const EVENTO_NO_AUTORIZADO = 'savora:no-autorizado'
// Se dispara cuando el cliente crea una reserva, para refrescar el contador del menú.
export const EVENTO_RESERVAS_CAMBIARON = 'savora:reservas-cambiaron'

export class ApiError extends Error {
  constructor(estado, problema) {
    super(problema?.detail || problema?.title || `Error ${estado}`)
    this.estado = estado
    this.problema = problema
  }
}

// --- Sesión (token JWT) -----------------------------------------------------
// sessionStorage: el token se pierde al cerrar la pestaña. Envuelto en try/catch por si
// el navegador bloquea el almacenamiento.

export function leerSesion() {
  try {
    const crudo = sessionStorage.getItem(CLAVE_SESION)
    return crudo ? JSON.parse(crudo) : null
  } catch {
    return null
  }
}

export function guardarSesion(sesion) {
  try {
    sessionStorage.setItem(CLAVE_SESION, JSON.stringify(sesion))
  } catch {
    /* sin almacenamiento: la sesión vive solo en memoria */
  }
}

export function borrarSesion() {
  try {
    sessionStorage.removeItem(CLAVE_SESION)
  } catch {
    /* nada que borrar */
  }
}

// El JWT que emite la API trae el claim "clienteId". Se lee aquí (sin verificar la firma, eso
// lo hace el servidor en cada petición) para saber qué perfil consultar.
export function clienteIdDeToken(token) {
  try {
    const carga = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(carga)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join(''),
    )
    const id = JSON.parse(json).clienteId
    return id === undefined || id === null ? null : id
  } catch {
    return null
  }
}

// --- Petición base ----------------------------------------------------------

async function solicitar(ruta, { metodo = 'GET', cuerpo, conToken = true } = {}) {
  const encabezados = { Accept: 'application/json, application/problem+json' }
  if (cuerpo !== undefined) encabezados['Content-Type'] = 'application/json'

  const sesion = leerSesion()
  if (conToken && sesion?.token) encabezados.Authorization = `Bearer ${sesion.token}`

  let respuesta
  try {
    respuesta = await fetch(`${API_URL}${ruta}`, {
      method: metodo,
      headers: encabezados,
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
    })
  } catch {
    throw new ApiError(0, { detail: 'No se pudo conectar con el servidor. Intente de nuevo en unos segundos.' })
  }

  const texto = await respuesta.text()
  let datos = null
  if (texto) {
    try {
      datos = JSON.parse(texto)
    } catch {
      datos = null
    }
  }

  if (!respuesta.ok) {
    // 401 en una ruta protegida: el token venció o no es válido. Se limpia la sesión y se
    // avisa a la aplicación para que redirija al login. (El 401 del propio login es otra cosa:
    // credenciales incorrectas, y se muestra como error del formulario.)
    if (respuesta.status === 401 && conToken) {
      borrarSesion()
      window.dispatchEvent(new Event(EVENTO_NO_AUTORIZADO))
    }
    throw new ApiError(respuesta.status, datos)
  }
  return datos
}

// Spring Data puede serializar la página plana o anidada en "page"; se normaliza aquí.
function normalizarPagina(datos) {
  const meta = datos.page ?? datos
  return {
    contenido: datos.content ?? [],
    numero: meta.number ?? 0,
    tamano: meta.size ?? (datos.content ?? []).length,
    totalElementos: meta.totalElements ?? (datos.content ?? []).length,
    totalPaginas: meta.totalPages ?? 1,
  }
}

// --- Operaciones de la API ----------------------------------------------------

export const api = {
  login: (username, password) =>
    solicitar('/auth/login', { metodo: 'POST', cuerpo: { username, password }, conToken: false }),

  listarPaquetes: async (pagina, tamano) => {
    const consulta = new URLSearchParams({
      estado: 'disponible',
      page: String(pagina),
      size: String(tamano),
      sort: 'horaLimiteRecogida,asc',
    })
    return normalizarPagina(await solicitar(`/api/v1/paquetes?${consulta}`))
  },

  reservar: (paqueteSorpresaId) =>
    solicitar('/api/v1/reservas', { metodo: 'POST', cuerpo: { paqueteSorpresaId } }),

  // estado es opcional: 'pendiente', 'recogido', etc.
  listarReservas: async (pagina, tamano, estado) => {
    const consulta = new URLSearchParams({
      page: String(pagina),
      size: String(tamano),
      sort: 'fechaHoraReserva,desc',
    })
    if (estado) consulta.set('estado', estado)
    return normalizarPagina(await solicitar(`/api/v1/reservas?${consulta}`))
  },

  // Cantidad de reservas pendientes (para el contador del menú).
  contarReservasPendientes: async () => {
    const pagina = await api.listarReservas(0, 1, 'pendiente')
    return pagina.totalElementos
  },

  obtenerCliente: (clienteId) => solicitar(`/api/v1/clientes/${clienteId}`),

  perfilImpacto: (clienteId) => solicitar(`/api/v1/clientes/${clienteId}/perfil-impacto`),
}