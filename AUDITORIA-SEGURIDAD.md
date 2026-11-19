# Auditoría de seguridad — Laboratorio 7 (actualizada para el proyecto final)

Repositorio **Savora** · EIF509 · revisión según lista de verificación (Sesión 14) y OWASP Top 10:2025 aplicable.  
Commits Lab 7 en rama `lab7-seguridad-robustez`: concurrencia `9774fad`, observabilidad `400e532`, pipeline `5dc5654`, auditoría `de15694`.  
Actualización del proyecto final: ver sección "Cambios posteriores al Lab 7".

## Lista de verificación (8 puntos)

| # | Verificación | Hallazgo | Riesgo | Corrección / evidencia | Commit |
|---|--------------|----------|--------|------------------------|--------|
| 1 | Autenticación y sesiones | API stateless con JWT (120 min); admin con sesión y formulario de login | Medio si la API quedara abierta | `anyRequest().authenticated()` en API; reservas con `ROLE_CLIENTE`, panel del negocio con `ROLE_NEGOCIO`, escrituras de catálogos con `ROLE_ADMIN` | Ya en `main` |
| 2 | Control de acceso (autorización) | Reservas y cierre deben atarse al dueño del token | Alto (IDOR) | `clienteId`/`negocioId` desde el token; `PropiedadRecursoException` → 403; cierre y reseñas validan propiedad en el servicio | `main` PR #16 |
| 3 | Secretos y configuración | JWT y BD no deben versionarse | Crítico | `SAVORA_JWT_SECRET`, `DB_*`, `MONGODB_URI` solo por entorno; seeder prod con `SAVORA_SEED_*` | `main` PR #16, #18 |
| 4 | Validación de entradas | Cuerpos JSON y formularios sin validar | Medio | Bean Validation en DTOs (incluida la reseña: 1 a 5 y comentario ≤ 500); `BindingResult` en admin; `ProblemasNegocioHandler` RFC 9457 | `main` Lab 5 |
| 5 | CSRF | El login del admin (`/login`) no enviaba token CSRF; se detectó en la revisión del Lab 6 | Medio | CSRF **activo** en la cadena Thymeleaf (`/login`, `/logout`, `/admin/**`), con prueba de integración que exige el token; la API `/api/**` y `/auth/**` queda sin CSRF por ser stateless con Bearer | `f8607d9` |
| 6 | CORS | Origen `*` permitiría robo de sesión desde sitios maliciosos | Medio | `SAVORA_CORS_ORIGINS` lista explícita; solo `/api/**` y `/auth/**` | `main` PR #18 |
| 7 | Transporte y cabeceras | Producción debe usar HTTPS | Medio | Render/Neon/Atlas exigen TLS; documentado en `docs/despliegue-lab6.md` | Documentación |
| 8 | Registro y monitoreo | Sin trazas de seguridad ni health acotado | Bajo | Logger `seguridad` (login fallido, 403, concurrencia); `/actuator/health` sin detalle; los conflictos de integridad registran la causa real en el log | `400e532` |

## OWASP Top 10:2025 (categorías aplicables)

| Categoría | Aplicación en Savora | Hallazgo | Riesgo | Mitigación | Commit |
|-----------|----------------------|----------|--------|------------|--------|
| A01 Broken Access Control | Reservas, cierre, reseñas, listados | Endpoints aceptaban `clienteId` ajeno | Alto | JWT + filtro por cliente autenticado; 403 por propiedad; una reseña solo la crea el dueño de una reserva recogida | PR #16 |
| A02 Security Misconfiguration | CORS, actuator, perfiles | Health podría filtrar componentes | Bajo | `show-details=never`; perfil `prod` separado | `400e532` |
| A04 Cryptographic Failures | JWT HS256, contraseñas | Secreto débil en repo | Alto | Secreto obligatorio por env; Render genera en prod; contraseñas con BCrypt | PR #16 |
| A05 Injection | JPA + validación | SQL injection vía JPA parametrizado | Bajo | Repositorios Spring Data; sin SQL dinámico concatenado | Verificado en código |
| A07 Authentication Failures | `/auth/login` | Fuerza bruta sin registro | Medio | BCrypt; eventos `login_fallido` en log estructurado | `400e532` |
| A09 Security Logging | Auditoría operativa | Sin logs de 401/403 | Bajo | `RegistroEventosSeguridad` + logback estructurado | `400e532` |
| A10 Mishandling of Exception | Errores Spring por defecto | Fugas de formato | Bajo | `ResponseEntityExceptionHandler` + Problem Details | PR #16 |

## Cambios posteriores al Lab 7

| Cambio | Riesgo considerado | Decisión |
|--------|--------------------|----------|
| Rol `ADMIN` y aprobación de negocios | Un negocio nuevo no debe publicar sin revisión | El registro crea el negocio en estado `pendiente`; solo un negocio `aprobado` puede publicar paquetes (`CuentaNoHabilitadaException` → 403); suspender o aprobar es solo del ADMIN |
| Pago simulado (`PasarelaPagoSimulada`) | Manejo de datos de tarjeta | **No es un pago real.** El número se valida con Luhn y no se guarda; solo se almacenan la marca, los últimos 4 dígitos y una referencia. Los datos de tarjeta no se escriben en logs |
| Carrito de varios paquetes | Reservar más de lo permitido o dejar reservas a medias | `POST /api/v1/reservas/carrito`: de 1 a 5 paquetes y todo o nada en una transacción; se mantiene el límite de reservas activas por cliente |
| Cierre automático (`@Scheduled`) | Pérdida o doble cierre de un paquete | Solo cierra paquetes `disponible` con hora límite vencida; un fallo en uno no detiene los demás; no corre en el perfil de pruebas |
| Rachas e insignias (MongoDB) | Manipulación del historial desde el cliente | El historial solo lo modifica el servidor al cerrar una reserva; ningún endpoint permite escribir el perfil. Los códigos de insignia son un `enum` (`CodigoInsignia`) validado en la capa de negocio; el validador `$jsonSchema` de `mongo-init` aplica solo en el entorno local, no en Atlas |
| Reseñas (MongoDB) | Reseñas falsas o repetidas | Solo el dueño de una reserva `recogida` califica; una por reserva con índice único; calificación 1 a 5 y comentario ≤ 500 caracteres; se publica solo el primer nombre del cliente |
| Recuperación de contraseña | Enumeración de correos | La API responde siempre 202, exista o no el correo; el token es de un solo uso |
| Lectura de datos de cliente | Un cliente podía leer los datos y el perfil de impacto de otro cambiando el id en la URL (IDOR) | `AccesoPropio`: un cliente solo consulta su propio id; el administrador, cualquiera; si no, 403. Prueba: `AccesoPropioTest` |

## Riesgos conocidos y aceptados

- Un JWT sigue válido hasta vencer (120 min) aunque se suspenda al usuario: el filtro no consulta la base.
- El pago es simulado y no hay reembolso cuando un paquete queda `no_recogido`.
- Mongo no participa en la transacción de PostgreSQL: se acepta consistencia eventual en el perfil de impacto y las reseñas.
- Los negocios no tienen recuperación de contraseña por correo.

## Concurrencia (complemento Lab 7)

| Ítem | Decisión |
|------|----------|
| Entidad | `PaqueteSorpresa` (`@Version`) |
| Operación contenciosa | `ReservaService.reservar` decrementa stock concurrente |
| Conflicto HTTP | 409 Problem Details «Conflicto de concurrencia» |
| Prueba | `PaqueteSorpresaOptimisticLockIT`, `ProblemasNegocioHandlerTest` (`9774fad`) |

## Pipeline y rama `main`

- Workflow `.github/workflows/ci.yml`: build Gradle (incluye JaCoCo ≥ 70 %), build SPA y despliegue en Render **solo en push a `main`**, mediante deploy hooks, si pasan los jobs `api` y `spa`.
- Secretos en GitHub: `RENDER_DEPLOY_HOOK_API`, `RENDER_DEPLOY_HOOK_SPA` (no documentar valores).
- **Protección de rama:** regla (ruleset) activa sobre `main` en GitHub: exige pull request y los checks `api` y `spa` antes de fusionar, y bloquea los force push (configuración del repositorio, no versionada).

## Cómo se comprobó lo sin hallazgo crítico

- **Injection:** revisión de repositorios y Specifications sin concatenación de SQL.
- **HTTPS:** URLs de despliegue usan `https://` en Render.
- **Pruebas de seguridad:** `AuthControllerIT`, `ReservaV1IT`, `PaqueteSorpresaV1IT`, `CierreV1IT` (401/403), `AdminMvcIT` (CSRF en el login del admin).