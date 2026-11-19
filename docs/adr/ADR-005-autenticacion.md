# ADR-005 · Estrategia de autenticación

**Estado y fecha:** aceptada (octubre de 2026) · **Responsables:** Yeilin Moya Baltodano, Nazareth Gómez Gómez

## Contexto

El sistema tiene dos clientes distintos: una SPA que consume una API REST y un panel administrativo con
plantillas Thymeleaf. Hay tres roles (`CLIENTE`, `NEGOCIO`, `ADMIN`) y datos que cada usuario solo debe ver o
modificar si son suyos. Los negocios nuevos no deben operar hasta que un administrador los apruebe.

## Decisión

- **API (`/api/**`, `/auth/**`): JWT sin estado.** `POST /auth/login` valida la contraseña (BCrypt) y devuelve un
  token HS256 con los claims `rol`, `clienteId` y `negocioId`. Un filtro lo lee en cada petición. El identificador
  del dueño se toma siempre del token, nunca del cuerpo de la petición.
- **Panel administrativo (`/admin/**`): sesión con formulario de login y protección CSRF**, solo para el rol `ADMIN`.
- **Tres cadenas de seguridad** separadas (API, documentación y actuator, panel), para que cada una aplique sus reglas.
- **Cuentas:** un negocio que se registra queda `pendiente` y no puede iniciar sesión hasta que lo aprueban. Un
  usuario `inactivo` o un negocio `suspendido` recibe 403 al iniciar sesión.
- **Secreto JWT** por variable de entorno (`SAVORA_JWT_SECRET`). Render lo genera en producción.
- **Escrituras sobre negocios, categorías, organizaciones y clientes** restringidas al rol `ADMIN`.

## Alternativas consideradas

1. **Sesiones con cookie también para la SPA:** se descartó porque exigiría manejar CSRF y cookies entre dos
   dominios distintos (SPA y API en Render).
2. **Un proveedor externo (OAuth2 / Keycloak / Auth0):** se descartó por la complejidad operativa y porque el
   alcance del curso no lo requiere.
3. **JWT también para el panel:** se descartó porque el panel usa formularios del servidor, y para eso la sesión con
   CSRF es lo natural.

## Consecuencias

- **Positivas:** la API escala sin estado y el aislamiento por dueño está en el diseño, no en validaciones sueltas.
  Los secretos nunca se versionan.
- **Negativas:** el filtro JWT no consulta la base de datos, así que el token de un usuario suspendido sigue
  siendo válido hasta que vence (120 minutos). Tampoco hay revocación de tokens ni recuperación de contraseña
  para las cuentas de negocio.
- **Neutras:** hay que mantener dos mecanismos de autenticación y probar ambos (401 en la API, 403 en el panel).