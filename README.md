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

4. Compilá y corré las pruebas:

```bash
./gradlew build
```

5. Levantá la aplicación:

```bash
./gradlew bootRun
```

6. La aplicación queda escuchando en `http://localhost:8080`. Flyway aplica las migraciones y el seeder de Mongo carga los datos de ejemplo automáticamente al iniciar.

## Endpoints de prueba

Con la aplicación corriendo:

```bash
curl http://localhost:8080/api/salud
curl http://localhost:8080/api/categorias
curl http://localhost:8080/api/clientes/1/perfil-impacto
curl http://localhost:8080/api/negocios/1/perfil-impacto
curl http://localhost:8080/actuator/health
```

## Estado del proyecto

En desarrollo — Laboratorio 2: capa de datos completa (PostgreSQL + MongoDB).