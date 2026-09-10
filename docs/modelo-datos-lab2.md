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
| `donacion` | Asignación de un paquete no reservado a una organización |

### Relaciones

- Un `negocio` publica muchos `paquete_sorpresa`.
- Un `paquete_sorpresa` pertenece a una `categoria`.
- Un `cliente` realiza muchas `reserva`.
- Una `reserva` corresponde a un solo `paquete_sorpresa`.
- Un `paquete_sorpresa` donado genera una `donacion` hacia `organizacion_comunitaria`.

### Normalización (3FN)

Las tablas base no repiten datos de sus dependientes: el correo del cliente vive solo en `cliente`, el tipo de comida solo en `categoria`, y cada paquete referencia esas tablas por FK. El historial de compras tampoco se guarda en `cliente`; se obtiene consultando `reserva` por `cliente_id`. Duplicarlo en otra tabla introduciría redundancia y haría difícil mantener consistencia cuando cambia el estado de una reserva.

### Restricciones de negocio

- Correo del cliente único.
- Precio con descuento menor al precio original.
- Estados válidos en paquetes y reservas.
- Un paquete solo puede tener una reserva activa (UNIQUE en `paquete_sorpresa_id`).

### Migraciones Flyway

- **V1** — tablas base: categoria, negocio, cliente, organizacion_comunitaria
- **V2** — núcleo transaccional: paquete_sorpresa, reserva
- **V3** — índices sobre consultas frecuentes (detalle abajo)
- **V4** — datos de ejemplo en las tablas relacionales
- **V5** — tabla `donacion` como entidad propia (detalle abajo)

#### Índices (V3)

`idx_paquete_negocio` acelera el panel del negocio: al listar los paquetes que publicó un comercio, PostgreSQL filtra por `negocio_id` en casi toda consulta de administración.

`idx_paquete_categoria` soporta el filtro del catálogo por tipo de comida. Sin índice, cada búsqueda por categoría recorrería la tabla completa de paquetes.

`idx_reserva_cliente` optimiza el historial de compras y el conteo de reservas activas de un cliente, operaciones centrales del proceso de reserva.

`idx_paquete_disponible` es parcial (`WHERE estado = 'disponible'`) porque el catálogo público solo muestra paquetes abiertos a reserva. Indexar todos los estados ocuparía espacio en filas que casi nunca se consultan juntas.

Los campos con UNIQUE (`cliente.correo`, `categoria.nombre`) ya traen índice implícito desde V1.

#### Tabla donacion (V5)

En V2 la donación era solo un estado del paquete más una FK suelta a la organización. Eso no permitía registrar cuándo se donó ni si la organización aceptó o rechazó. Al extraer `donacion` como tabla propia, el paquete conserva su ciclo de vida (`disponible`, `reservado`, `donado`, etc.) y la asignación a la ONG queda en un registro con `fecha_donacion` y `estado`, coherente con el proceso 2 de la propuesta de dominio. El índice en `organizacion_comunitaria_id` facilita consultar qué donaciones recibió cada comedor.

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