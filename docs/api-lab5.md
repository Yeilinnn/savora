# API REST — Laboratorio 5 (parte Nazareth)

EIF509 · Savora · II Ciclo 2026

## Alcance de esta rama

Implementado bajo **`/api/v1`** (sin JWT aún — pendiente Yeilin):

| Área | Endpoints |
|------|-----------|
| Salud | `GET /api/v1/salud` |
| Reservas (proceso 1) | `POST /api/v1/reservas`, `GET /api/v1/reservas`, `GET /api/v1/reservas/{id}` |
| Clientes | `GET/POST /api/v1/clientes`, `GET /api/v1/clientes/{id}` |
| Negocios | `GET/POST /api/v1/negocios`, `GET /api/v1/negocios/{id}` |
| Categorías | `GET/POST /api/v1/categorias`, `GET /api/v1/categorias/{id}` |
| Organizaciones | `GET/POST /api/v1/organizaciones-comunitarias`, `GET .../{id}` |
| Perfil impacto | `GET /api/v1/clientes/{id}/perfil-impacto`, `GET /api/v1/negocios/{id}/perfil-impacto` |

## Errores

`ProblemasNegocioHandler` devuelve **Problem Details** (RFC 9457):

- **400** — validación `@Valid`
- **404** — `RecursoNoEncontradoException`
- **409** — conflicto de integridad (p. ej. duplicado)
- **422** — reglas de negocio

## Paginación y filtros (reservas)

`GET /api/v1/reservas?clienteId=&estado=&page=&size=&sort=`

Filtros vía `ReservaEspecificaciones` (cliente + estado).

## OpenAPI

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Pendiente Yeilin

JWT (`POST /auth/login`), roles, paquetes/cierre, IT **401/403**, mitad `.http` (auth, paquetes, cierre).

## Revisión final (ambas)

Colección `.http` completa, Swagger con todos los endpoints, IT 401/403 cuando exista seguridad.
