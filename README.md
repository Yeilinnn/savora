# Savora

Plataforma que conecta negocios pequeños de comida (panaderías, sodas, restaurantes) con clientes que quieren comprar su excedente diario a precio reducido, evitando que se pierda. Si un paquete no se reserva a tiempo, el sistema prioriza asignarlo a organizaciones comunitarias como donación. El cliente puede consultar su historial de compras a partir de sus reservas.

Cada cliente y cada negocio acumulan un historial de impacto (comida rescatada, reservas cumplidas, paquetes donados). Un cliente con buen historial puede reservar más paquetes a la vez; un negocio que dona en vez de perder su excedente aparece mejor posicionado en el catálogo. El cliente también puede dejar una reseña de cada paquete que recoge.

## Sistema publicado

| Qué | Dirección |
|---|---|
| SPA (clientes y negocios) | <https://savora-spa.onrender.com> |
| Inicio de sesión SPA | <https://savora-spa.onrender.com/#/login> |
| API | <https://savora-api-ff0z.onrender.com> |
| Swagger UI | <https://savora-api-ff0z.onrender.com/swagger-ui.html> |
| Panel administrativo (ADMIN) | <https://savora-api-ff0z.onrender.com/login> |
| Salud | <https://savora-api-ff0z.onrender.com/actuator/health> |

También figuran en [docs/despliegue-lab6.md](docs/despliegue-lab6.md) y [docs/diagrama-despliegue.md](docs/diagrama-despliegue.md).

El plan gratuito de Render suspende los servicios tras 15 minutos sin tráfico; la primera petición tarda cerca de un minuto en despertarlos.

## Integrantes

- Yeilin Moya Baltodano, 504550568
- Nazareth Gomez Gomez, 504430491

## Curso

EIF509 Desarrollo de Aplicaciones Basadas en Web · Universidad Nacional · II Ciclo 2026

## Stack

- Java 21
- Spring Boot 4.1.0
- Gradle
- PostgreSQL (Flyway) + MongoDB (persistencia políglota)
- Arquitectura organizada en capas: `presentation`, `business`, `data`, `config`

## Documentación

- [**Documento de arquitectura** (C4, ADR, modelo de datos, seguridad y lecciones)](docs/arquitectura.md)
- [Propuesta de dominio](docs/propuesta-dominio.md)
- [Diagrama de arquitectura y decisión SQL/NoSQL](docs/diagrama-arquitectura.md)
- [Modelo de datos — Laboratorio 2](docs/modelo-datos-lab2.md)
- [Persistencia — Laboratorio 3](docs/persistencia-lab3.md)
- [Capa de negocio — Laboratorio 4](docs/negocio-lab4.md)
- [API REST — Laboratorio 5](docs/api-lab5.md)
- [Despliegue — Laboratorio 6](docs/despliegue-lab6.md)
- [Diagrama de despliegue](docs/diagrama-despliegue.md)
- [Auditoría de seguridad (Lab 7)](AUDITORIA-SEGURIDAD.md)
- ADR: [001 Versión de Spring Boot](docs/adr/ADR-001-version-spring-boot.md) ·
  [002 Dominio y alcance](docs/adr/ADR-002-dominio-y-alcance.md) ·
  [003 SQL / NoSQL](docs/adr/ADR-003-sql-nosql.md) ·
  [004 Patrones de diseño](docs/adr/ADR-004-patrones-de-diseno.md) ·
  [005 Autenticación](docs/adr/ADR-005-autenticacion.md) ·
  [006 Plataforma y despliegue](docs/adr/ADR-006-plataforma-y-despliegue.md)

## Cómo levantar el proyecto

1. Cloná el repositorio:

```bash
git clone https://github.com/Yeilinnn/savora.git
```

2. Entrá a la carpeta del proyecto:

```bash
cd savora
```

3. Levantá PostgreSQL y MongoDB:

```bash
docker compose up -d
```

4. Compilá, probá y verificá cobertura JaCoCo (≥ 70% en `business`):

```bash
./gradlew build
```

5. Levantá la aplicación (perfil `dev` incluye JWT local y usuarios de prueba):

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

6. La aplicación queda en `http://localhost:8080`. SPA: ver carpeta `spa/` (`npm ci && npm run dev`).

Usuarios de prueba del perfil `dev` (solo en una base local vacía):

| Usuario | Contraseña | Rol | Dónde entra |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Panel: `http://localhost:8080/login` |
| `negocio1` / `negocio2` | `negocio123` | NEGOCIO | SPA |
| `cliente1` | `cliente123` | CLIENTE | SPA |

En `dev` se crean además algunos paquetes con hora límite relativa a la hora actual, para poder demostrar la reserva sin crear datos a mano.

**Concurrencia:** dos clientes pueden reservar el mismo paquete casi al mismo tiempo y ambos leen el stock antes de que uno confirme; por eso `PaqueteSorpresa` usa `@Version` (bloqueo optimista) y un conflicto responde **409** para que el cliente reintente con datos frescos.

### Variables de entorno (producción — solo nombres)

| Variable | Descripción |
|---|---|
| `SAVORA_JWT_SECRET` | Secreto HS256 (≥ 32 bytes); en Render se genera automáticamente |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | PostgreSQL (Neon) |
| `MONGODB_URI` | MongoDB Atlas |
| `SAVORA_CORS_ORIGINS` | URL HTTPS de la SPA (sin `/` final) |
| `SAVORA_SEED_NEGOCIO_PASSWORD` | Contraseña inicial de usuarios negocio |
| `SAVORA_SEED_CLIENTE_PASSWORD` | Contraseña inicial de `cliente1` |
| `SAVORA_SEED_ADMIN_PASSWORD` | Contraseña del usuario `admin` del panel (si no se define, no se crea) |
| `SAVORA_SEED_DEMO` | `true` para crear paquetes de demostración con fechas relativas en producción (opcional) |
| `SAVORA_SPA_URL` | URL de la SPA, para los enlaces de los correos |
| `SAVORA_RESEND_API_KEY`, `SAVORA_MAIL_FROM` | Correo de recuperación de contraseña (opcional) |
| `VITE_API_URL` | URL pública de la API (build de la SPA) |
| `RENDER_DEPLOY_HOOK_API` / `RENDER_DEPLOY_HOOK_SPA` | Hooks de despliegue Render (solo GitHub Secrets del pipeline) |

## Endpoints de prueba

Con la aplicación corriendo:

```bash
curl http://localhost:8080/api/v1/salud
curl http://localhost:8080/swagger-ui.html
curl http://localhost:8080/api/v1/clientes/1/perfil-impacto
curl http://localhost:8080/actuator/health
```

## Estado del proyecto

Sistema completo y desplegado: API REST, panel administrativo Thymeleaf (aprobación de negocios, usuarios, categorías y organizaciones), SPA React (catálogo, carrito, pago simulado, reservas, impacto con rachas e insignias, reseñas), cierre automático de paquetes y despliegue desde la integración continua en Render, Neon y Atlas. Ver [arquitectura.md](docs/arquitectura.md) y [despliegue-lab6.md](docs/despliegue-lab6.md).