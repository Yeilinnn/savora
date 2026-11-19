# Diagrama de despliegue — Laboratorio 6

## Sistema publicado

| Qué | Dirección |
|---|---|
| API | <https://savora-api-ff0z.onrender.com> |
| SPA | <https://savora-spa.onrender.com> |
| Swagger UI | <https://savora-api-ff0z.onrender.com/swagger-ui.html> |
| Panel administrativo | <https://savora-api-ff0z.onrender.com/login> |

El nombre del servicio de la API lleva el sufijo `-ff0z` porque Render lo agrega cuando el nombre ya está tomado.


```mermaid
flowchart LR
    subgraph clientes ["Clientes"]
        SPA["SPA React<br/>(savora-spa)"]
        Admin["Navegador admin<br/>/login · Thymeleaf"]
    end

    subgraph render ["Render.com"]
        API["savora-api<br/>Spring Boot · Docker"]
    end

    subgraph datos ["Servicios gestionados"]
        Neon["PostgreSQL<br/>(Neon)"]
        Atlas["MongoDB<br/>(Atlas)"]
        Resend["Resend<br/>(correo HTTP)"]
    end

    subgraph ci ["GitHub"]
        Actions["GitHub Actions<br/>compila · prueba · cobertura"]
    end

    SPA -->|"HTTPS + JWT<br/>VITE_API_URL"| API
    Admin -->|"HTTPS + sesión + CSRF"| API
    API -->|"DB_URL / DB_USER / DB_PASSWORD"| Neon
    API -->|"MONGODB_URI"| Atlas
    API -->|"SAVORA_RESEND_API_KEY"| Resend
    Actions -->|"deploy hooks (GitHub Secrets)<br/>solo desde main y si todo pasa"| render
```

## Variables de entorno (solo nombres)

| Servicio | Variable | Uso |
|---|---|---|
| savora-api | `SPRING_PROFILES_ACTIVE` | Perfil `prod` |
| savora-api | `SAVORA_JWT_SECRET` | Firma JWT (generado en Render) |
| savora-api | `DB_URL`, `DB_USER`, `DB_PASSWORD` | PostgreSQL (Neon) |
| savora-api | `MONGODB_URI` | MongoDB Atlas |
| savora-api | `SAVORA_CORS_ORIGINS` | Origen HTTPS de la SPA (sin `/` final) |
| savora-api | `SAVORA_SEED_NEGOCIO_PASSWORD` | Contraseña de `negocio1` / `negocio2` |
| savora-api | `SAVORA_SEED_CLIENTE_PASSWORD` | Contraseña de `cliente1` (SPA) |
| savora-api | `SAVORA_SEED_ADMIN_PASSWORD` | Contraseña del usuario `admin` (panel) |
| savora-api | `SAVORA_SPA_URL` | URL de la SPA para los enlaces de los correos |
| savora-api | `SAVORA_RESEND_API_KEY`, `SAVORA_MAIL_FROM` | Correo de recuperación de contraseña (opcional) |
| GitHub Secrets | `RENDER_DEPLOY_HOOK_API`, `RENDER_DEPLOY_HOOK_SPA` | Hooks de despliegue, solo en el pipeline |
| savora-spa | `VITE_API_URL` | URL pública de la API (sin `/` final) |

Guía paso a paso: [despliegue-lab6.md](despliegue-lab6.md).