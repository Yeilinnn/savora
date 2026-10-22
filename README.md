# Savora

Plataforma que conecta negocios pequeños de comida (panaderías, sodas, restaurantes) con clientes que quieren comprar su excedente diario a precio reducido, evitando que se pierda. Si un paquete no se reserva a tiempo, el sistema prioriza asignarlo a organizaciones comunitarias como donación. El cliente puede consultar su historial de compras a partir de sus reservas.

Cada cliente y cada negocio acumulan un historial de impacto (comida rescatada, reservas cumplidas, paquetes donados). Un cliente con buen historial puede reservar más paquetes a la vez; un negocio que dona en vez de perder su excedente aparece mejor posicionado en el catálogo. El cliente también puede dejar una reseña de cada paquete que recoge.

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

- [Propuesta de dominio](docs/propuesta-dominio.md)
- [Diagrama de arquitectura y decisión SQL/NoSQL](docs/diagrama-arquitectura.md)
- [Modelo de datos — Laboratorio 2](docs/modelo-datos-lab2.md)
- [Persistencia — Laboratorio 3](docs/persistencia-lab3.md)
- [Capa de negocio — Laboratorio 4](docs/negocio-lab4.md)
- [API REST — Laboratorio 5](docs/api-lab5.md)
- [Despliegue — Laboratorio 6](docs/despliegue-lab6.md)
- [Diagrama de despliegue](docs/diagrama-despliegue.md)
- [ADR-001: Versión de Spring Boot](docs/adr/ADR-001-version-spring-boot.md)

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

6. La aplicación queda en `http://localhost:8080`. Módulo admin: `http://localhost:8080/login` (`negocio1` / `negocio123`). SPA: ver carpeta `spa/` (`npm ci && npm run dev`).

### Variables de entorno (producción — solo nombres)

| Variable | Descripción |
|---|---|
| `SAVORA_JWT_SECRET` | Secreto HS256 (≥ 32 bytes); en Render se genera automáticamente |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | PostgreSQL (Neon) |
| `MONGODB_URI` | MongoDB Atlas |
| `SAVORA_CORS_ORIGINS` | URL HTTPS de la SPA (sin `/` final) |
| `SAVORA_SEED_NEGOCIO_PASSWORD` | Contraseña inicial de usuarios negocio |
| `SAVORA_SEED_CLIENTE_PASSWORD` | Contraseña inicial de `cliente1` |
| `VITE_API_URL` | URL pública de la API (build de la SPA) |

## Endpoints de prueba

Con la aplicación corriendo:

```bash
curl http://localhost:8080/api/v1/salud
curl http://localhost:8080/swagger-ui.html
curl http://localhost:8080/api/v1/clientes/1/perfil-impacto
curl http://localhost:8080/actuator/health
```

## Estado del proyecto

Laboratorio 6: API + módulo administrativo Thymeleaf + SPA React + despliegue (Render/Neon/Atlas). Ver [despliegue-lab6.md](docs/despliegue-lab6.md).