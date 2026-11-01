# Auditoría de seguridad — Laboratorio 7

Repositorio **Savora** · EIF509 · revisión según lista de verificación (Sesión 14) y OWASP Top 10:2025 aplicable.  
Commits Lab 7 en rama `lab7-seguridad-robustez`: concurrencia `9774fad`, observabilidad `400e532`, pipeline `5dc5654`, auditoría `de15694`.

## Lista de verificación (8 puntos)

| # | Verificación | Hallazgo | Riesgo | Corrección / evidencia | Commit |
|---|--------------|----------|--------|------------------------|--------|
| 1 | Autenticación y sesiones | API stateless con JWT; admin con sesión form login | Medio si la API quedara abierta | `anyRequest().authenticated()` en API; reservas con `ROLE_CLIENTE`; login admin con CSRF | Ya en `main` (Lab 5–6) |
| 2 | Control de acceso (autorización) | Reservas y cierre deben atarse al dueño del token | Alto (IDOR) | `clienteId`/`negocioId` desde token; `PropiedadRecursoException` → 403; cierre valida negocio en servicio | `main` PR #16 |
| 3 | Secretos y configuración | JWT y BD no deben versionarse | Crítico | `SAVORA_JWT_SECRET`, `DB_*`, `MONGODB_URI` solo por entorno; seeder prod con `SAVORA_SEED_*` | `main` PR #16, #18 |
| 4 | Validación de entradas | Cuerpos JSON y formularios sin validar | Medio | Bean Validation en DTOs; `BindingResult` en admin; `ProblemasNegocioHandler` RFC 9457 | `main` Lab 5 |
| 5 | CSRF | Mutaciones admin sin token | Medio | CSRF activo en cadena `/admin/**` y `/login`; API `/api/**` sin CSRF (Bearer) | `main` PR #18 |
| 6 | CORS | Origen `*` permitiría robo de sesión desde sitios maliciosos | Medio | `SAVORA_CORS_ORIGINS` lista explícita; solo `/api/**` y `/auth/**` | `main` PR #18 |
| 7 | Transporte y cabeceras | Producción debe usar HTTPS | Medio | Render/Neon/Atlas exigen TLS; documentado en `docs/despliegue-lab6.md` | Documentación |
| 8 | Registro y monitoreo | Sin trazas de seguridad ni health acotado | Bajo | Logger `seguridad` (login fallido, 403, concurrencia); `/actuator/health` sin detalle | `400e532` |

## OWASP Top 10:2025 (categorías aplicables)

| Categoría | Aplicación en Savora | Hallazgo | Riesgo | Mitigación | Commit |
|-----------|----------------------|----------|--------|------------|--------|
| A01 Broken Access Control | Reservas, cierre, listados | Endpoints aceptaban `clienteId` ajeno | Alto | JWT + filtro por cliente autenticado; 403 por propiedad | PR #16 |
| A02 Security Misconfiguration | CORS, actuator, perfiles | Health podría filtrar componentes | Bajo | `show-details=never`; perfil `prod` separado | `400e532` |
| A04 Cryptographic Failures | JWT HS256 | Secreto débil en repo | Alto | Secreto obligatorio por env; Render genera en prod | PR #16 |
| A05 Injection | JPA + validación | SQL injection vía JPA parametrizado | Bajo | Repositorios Spring Data; sin SQL dinámico concatenado | Verificado en código |
| A07 Authentication Failures | `/auth/login` | Fuerza bruta sin registro | Medio | BCrypt; eventos `login_fallido` en log estructurado | `400e532` |
| A09 Security Logging | Auditoría operativa | Sin logs de 401/403 | Bajo | `RegistroEventosSeguridad` + logback estructurado | `400e532` |
| A10 Mishandling of Exception | Errores Spring por defecto | Fugas de formato | Bajo | `ResponseEntityExceptionHandler` + Problem Details | PR #16 |

## Concurrencia (complemento Lab 7)

| Ítem | Decisión |
|------|----------|
| Entidad | `PaqueteSorpresa` (`@Version`) |
| Operación contenciosa | `ReservaService.reservar` decrementa stock concurrente |
| Conflicto HTTP | 409 Problem Details «Conflicto de concurrencia» |
| Prueba | `PaqueteSorpresaOptimisticLockIT`, `ProblemasNegocioHandlerTest` (`9774fad`) |

## Pipeline y rama `main`

- Workflow `.github/workflows/ci.yml`: build Gradle (incluye JaCoCo ≥ 70 %), build SPA, despliegue Render **solo en push a `main`** si pasan los jobs.
- Secretos en GitHub: `RENDER_DEPLOY_HOOK_API`, `RENDER_DEPLOY_HOOK_SPA` (no documentar valores).
- **Protección de rama:** activar en GitHub → Settings → Branches → `main`: require status checks (`api`, `spa`) antes de merge (configuración del repositorio, no versionada).

## Cómo se comprobó lo sin hallazgo crítico

- **Injection:** revisión de repositorios y Specifications sin concatenación de SQL.
- **HTTPS:** URLs de despliegue usan `https://` en Render.
- **Pruebas de seguridad:** `AuthControllerIT`, `ReservaV1IT`, `PaqueteSorpresaV1IT`, `CierreV1IT` (401/403).
