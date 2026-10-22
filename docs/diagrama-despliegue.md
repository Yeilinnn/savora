# Diagrama de despliegue — Laboratorio 6

Complete las URLs reales de su entrega en Render antes de subir al aula virtual.

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
    end

    SPA -->|"HTTPS + JWT<br/>VITE_API_URL"| API
    Admin -->|"HTTPS + sesión + CSRF"| API
    API -->|"DB_URL / DB_USER / DB_PASSWORD"| Neon
    API -->|"MONGODB_URI"| Atlas
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
| savora-spa | `VITE_API_URL` | URL pública de la API (sin `/` final) |

Guía paso a paso: [despliegue-lab6.md](despliegue-lab6.md).
