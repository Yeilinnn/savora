# Savora

Plataforma que conecta negocios pequeños de comida (panaderías, sodas, restaurantes) con clientes que quieren comprar su excedente diario a precio reducido, evitando que se pierda. Si un paquete no se reserva a tiempo, el sistema prioriza asignarlo a organizaciones comunitarias como donación.

## Integrantes

- Yeilin Moya Baltodano, 504550568
- Nazareth Gomez Gomez, 504430491

## Curso

EIF509 Desarrollo de Aplicaciones Basadas en Web · Universidad Nacional · II Ciclo 2026

## Stack

- Java 21
- Spring Boot 4.1.0
- Gradle
- Arquitectura organizada en capas: `presentation`, `business`, `data`, `config`

## Documentación

- [Propuesta de dominio](docs/propuesta-dominio.md)
- [Diagrama de arquitectura](docs/diagrama-arquitectura.md)
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

3. Compilá y corré las pruebas:

```bash
./gradlew build
```

4. Levantá la aplicación:

```bash
./gradlew bootRun
```

5. La aplicación queda escuchando en `http://localhost:8080`.

## Endpoints de prueba

Con la aplicación corriendo:

```bash
curl http://localhost:8080/api/salud
curl http://localhost:8080/api/categorias
curl http://localhost:8080/actuator/health
```

## Estado del proyecto

En desarrollo — Laboratorio 1: esqueleto inicial por capas.
