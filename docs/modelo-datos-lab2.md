# Modelo de datos — Laboratorio 2

EIF509 · Savora · II Ciclo 2026

## Persistencia políglota

El núcleo transaccional de Savora vive en **PostgreSQL**: clientes, negocios, paquetes, reservas y organizaciones comunitarias. La parte documental (historial de impacto y reseñas) vive en **MongoDB**.

**Dos "historiales" distintos, para no confundirlos:**

- **Historial de compras** — qué reservó un cliente. Ya existe como dato: es una consulta sobre la tabla `reserva`. Vive en PostgreSQL. Ver la nota en "Relaciones" más abajo.
- **Historial de impacto** — kg rescatados, ratios, insignias. Es el subdominio documental de este laboratorio. Vive en MongoDB, como `perfil_impacto`. Ver "Subdominio MongoDB" más abajo.

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

**Nota:** el "historial de compras" del cliente (mencionado en la propuesta de dominio) no es una columna de `cliente` — se obtiene consultando `reserva` filtrado por `cliente_id`. Guardarlo aparte duplicaría datos que ya viven en `reserva`, violando 3FN.

### Restricciones de negocio

- Correo del cliente único.
- Precio con descuento menor al precio original.
- Estados válidos en paquetes y reservas.
- Un paquete solo puede tener una reserva activa (UNIQUE en `paquete_sorpresa_id`).

### Migraciones Flyway

- **V1** — tablas base: categoria, negocio, cliente, organizacion_comunitaria
- **V2** — núcleo transaccional: paquete_sorpresa, reserva
- **V3** — índices justificados: FK que se consultan seguido (`negocio_id`, `categoria_id` en `paquete_sorpresa`; `cliente_id` en `reserva`) y un índice parcial sobre `paquete_sorpresa.estado` para el catálogo público (`WHERE estado = 'disponible'`)
- **V4** — datos de ejemplo (seeds) en las 6 tablas relacionales
- **V5** — extrae la donación como entidad propia: tabla `donacion` (fecha, estado, referencia a la organización), y le quita a `paquete_sorpresa` la columna suelta que tenía antes. Índice en `donacion.organizacion_comunitaria_id`.

## Subdominio MongoDB

**Colección elegida:** `perfil_impacto` — el historial de impacto de cada cliente y cada negocio, con un sistema de insignias que le da un propósito funcional (no solo informativo).

### Propósito de las insignias

El historial no es solo informativo: alimenta reglas de negocio ya existentes.

- **Cliente:** el límite de reservas activas simultáneas (regla del Laboratorio 1) se ajusta según el ratio `reservasRecogidas` / `reservasNoRecogidas`. Buen historial → sube el límite.
- **Negocio:** el ratio `paquetesDonados` / `paquetesPerdidos` influye en el orden de aparición en el catálogo.

### Criterios de diseño (Sesión 4)

| Pregunta | Respuesta para Savora |
|---|---|
| ¿Cómo se lee el 90% del tiempo? | Junto al perfil de su dueño → se incrusta todo en un solo documento |
| ¿Cuánto crece en el peor caso? | Sin límite fijo (insignias nuevas con el tiempo) → no encaja en tabla relacional rígida |
| ¿Quién más lo necesita? | Solo su propio dueño → no hay razón para referenciar |

**Conclusión:** documento único por dueño, insignias incrustadas como arreglo dentro del mismo documento.

### Estructura del documento

```json
{
  "_id": "ObjectId",
  "propietarioId": "12",
  "tipoPropietario": "CLIENTE",
  "kgRescatados": 18.5,
  "reservasRecogidas": 14,
  "reservasNoRecogidas": 1,
  "paquetesDonados": 0,
  "paquetesPerdidos": 0,
  "insignias": [
    { "codigo": "PRIMER_RESCATE", "fechaObtenida": "2026-08-10T18:30:00Z" },
    { "codigo": "GUARDIAN_DEL_BARRIO", "fechaObtenida": "2026-08-20T19:00:00Z" }
  ]
}
```

`propietarioId` referencia por id (no FK real, no hay integridad referencial entre bases distintas) al `id` de `cliente` o `negocio` en PostgreSQL, según `tipoPropietario`.

### Insignias definidas

| Código | Para quién | Se otorga cuando |
|---|---|---|
| `PRIMER_RESCATE` | Cliente | Recoge su primera reserva |
| `GUARDIAN_DEL_BARRIO` | Cliente | Acumula 10 reservas recogidas |
| `RACHA_3_SEMANAS` | Cliente | Recoge al menos 1 paquete por semana, 3 semanas seguidas |
| `CERO_DESPERDICIO` | Negocio | Ratio `paquetesDonados`/`paquetesPerdidos` ≥ 0.8 con al menos 10 paquetes publicados |

`RACHA_3_SEMANAS` queda definida en el modelo pero su otorgamiento automático no está implementado: requiere agrupar reservas recogidas por semana, algo que depende del flujo completo de reservas de un laboratorio posterior. Las otras tres sí se otorgan automáticamente.

### Implementación

- `PerfilImpacto` (documento), `Insignia` (incrustada), `TipoPropietario` y `CodigoInsignia` (enums) — paquete `data`
- `PerfilImpactoRepository` — `MongoRepository`, paquete `data`
- `PerfilImpactoService` — calcula el límite de reservas y otorga insignias, paquete `business`
- `PerfilImpactoController` — `GET /api/clientes/{id}/perfil-impacto`, `GET /api/negocios/{id}/perfil-impacto`, paquete `presentation`
- `MongoSeeder` — siembra `perfil_impacto` al iniciar la aplicación, con los mismos ids de cliente/negocio que `V4__datos_seed.sql`, paquete `config`

## Reseña

Segunda entidad del subdominio documental, definida en el modelo pero sin código todavía.

**Qué es:** calificación que un cliente deja sobre un paquete que recogió.

**Criterios de diseño (Sesión 4):**

| Pregunta | Respuesta para Savora |
|---|---|
| ¿Cómo se lee el 90% del tiempo? | Junto al paquete o al negocio que se está mostrando → se incrusta |
| ¿Cuánto crece en el peor caso? | Sin límite, una por cada reserva recogida | 
| ¿Quién más lo necesita? | Se muestra en el contexto del paquete/negocio, no requiere transacción con otras tablas |

**Estructura del documento:**

```json
{
  "_id": "ObjectId",
  "clienteId": "1",
  "paqueteSorpresaId": "1",
  "calificacion": 5,
  "comentario": "Pan fresco, llegué justo antes de cerrar.",
  "fecha": "2026-08-20T19:15:00Z"
}
```

**Estado:** entidad y estructura definidas; sin clase `@Document`, repositorio, servicio ni controller — queda para un laboratorio posterior.

## Cómo levantar las bases de datos

```bash
docker compose up -d
bash ./gradlew bootRun
```

Flyway aplica las migraciones al iniciar la aplicación.