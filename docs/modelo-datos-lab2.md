# Modelo de datos — Laboratorio 2

EIF509 · Savora · II Ciclo 2026

## Persistencia políglota

El núcleo transaccional de Savora vive en **PostgreSQL**: clientes, negocios, paquetes, reservas y organizaciones comunitarias. La parte documental (historial de impacto) irá en **MongoDB**.

## Modelo relacional (PostgreSQL)

### Tablas

| Tabla | Descripción |
|---|---|
| `categoria` | Tipos de comida (panadería, comida preparada, etc.) |
| `negocio` | Comercios que publican excedente |
| `cliente` | Usuarios que reservan paquetes |
| `organizacion_comunitaria` | Comedores u ONG que reciben donaciones |
| `paquete_sorpresa` | Excedente publicado por un negocio |
| `reserva` | Vínculo entre cliente y paquete reservado |

### Relaciones

- Un `negocio` publica muchos `paquete_sorpresa`.
- Un `paquete_sorpresa` pertenece a una `categoria`.
- Un `cliente` realiza muchas `reserva`.
- Una `reserva` corresponde a un solo `paquete_sorpresa`.
- Un `paquete_sorpresa` donado referencia a `organizacion_comunitaria`.

### Restricciones de negocio

- Correo del cliente único.
- Precio con descuento menor al precio original.
- Estados válidos en paquetes y reservas.
- Un paquete solo puede tener una reserva activa (UNIQUE en `paquete_sorpresa_id`).

### Migraciones Flyway

- **V1** — tablas base: categoria, negocio, cliente, organizacion_comunitaria
- **V2** — núcleo transaccional: paquete_sorpresa, reserva
- **V3** — (pendiente) índices justificados
- **V4** — (pendiente) datos de ejemplo

## Subdominio MongoDB (pendiente)

**Colección candidata:** historial de impacto (kg rescatados, donaciones, bitácora diaria).

### Criterios de diseño (Sesión 4)

| Pregunta | Respuesta para Savora |
|---|---|
| ¿Cómo se lee el 90% del tiempo? | Junto al perfil del negocio o cliente → incrustar eventos diarios |
| ¿Cuánto crece? | La bitácora crece sin límite → referenciar por `negocio_id` / `cliente_id` |
| ¿Quién más lo necesita? | Admin para reportes → colección separada, referenciada desde PostgreSQL |

## Cómo levantar las bases de datos

```bash
docker compose up -d
bash ./gradlew bootRun
```

Flyway aplica las migraciones al iniciar la aplicación.
