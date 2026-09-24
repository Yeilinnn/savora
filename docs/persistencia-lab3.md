# Persistencia — Laboratorio 3

EIF509 · Savora · II Ciclo 2026

## Entidades JPA

| Entidad PostgreSQL | Clase JPA | Relaciones LAZY |
|---|---|---|
| categoria | Categoria | — |
| negocio | Negocio | — |
| cliente | Cliente | — |
| organizacion_comunitaria | OrganizacionComunitaria | — |
| paquete_sorpresa | PaqueteSorpresa | negocio, categoria |
| reserva | Reserva | cliente, paqueteSorpresa |
| donacion | Donacion | paqueteSorpresa, organizacionComunitaria |
| perfil_impacto | PerfilImpacto | MongoDB (PerfilImpactoRepository) |

Configuración: `ddl-auto=validate` contra migraciones Flyway V1–V5.

### Por qué LAZY en cada tipo de relación

**`@ManyToOne` (negocio y categoria en PaqueteSorpresa; cliente en Reserva; organizacionComunitaria en Donacion):** son las relaciones que más aparecen en listados (el catálogo de paquetes, el historial de un cliente). Si fueran EAGER, un `SELECT` que trae N paquetes arrastraría automáticamente N cargas de `Negocio` y N de `Categoria`, aunque la vista solo necesite, por ejemplo, el nombre del paquete — el costo de I/O crece con cada fila sin que el código lo pida explícitamente. LAZY deja que cada caso de uso decida: `JOIN FETCH` puntual cuando sí necesita la relación (como en `findByNegocioIdConCategoria`), o ninguna carga extra cuando no.

**`@OneToOne` propietario (paqueteSorpresa en Reserva y en Donacion):** aunque el lado propietario de un `@OneToOne` sí puede ser perezoso de verdad, en la práctica casi todo consumo de una `Reserva` o `Donacion` necesita saber a qué paquete pertenece, así que suele acompañarse de un `JOIN FETCH` explícito (ver `findPendientesConPaquete`). LAZY por defecto evita pagar ese costo en los pocos casos donde no se necesita.

**`@OneToOne(mappedBy=...)` — por qué ya NO existen en `PaqueteSorpresa`:** originalmente `PaqueteSorpresa` tenía el lado inverso hacia `reserva` y `donacion`. El lado no-propietario de un `@OneToOne` no puede ser perezoso de verdad sin instrumentación de bytecode: Hibernate necesita consultar la tabla relacionada para saber si existe una fila, sin importar el `FetchType` declarado. Eso convertía cada carga de un `PaqueteSorpresa` en 1 + 2 consultas (una por `reserva`, una por `donacion`) — ver "Problema N+1" abajo. Como nada en el código consumía esos getters, se eliminaron por completo: quien necesite saber si un paquete tiene reserva o donación consulta desde `ReservaRepository`/`DonacionRepository`, que son quienes tienen la llave foránea real.

## Consultas de negocio

### JPQL 1 — Paquetes disponibles para reservar

**Método:** `PaqueteSorpresaRepository.findDisponibles`

Regla: solo paquetes con estado `disponible` y hora límite futura.

```sql
SELECT p1_0.id, p1_0.cantidad, ...
FROM paquete_sorpresa p1_0
WHERE p1_0.estado = 'disponible'
  AND p1_0.hora_limite_recogida > ?
```

### JPQL 2 — Reservas pendientes de un cliente

**Método:** `ReservaRepository.findPendientesConPaquete`

Regla: historial activo del cliente con paquete y negocio cargados.

```sql
SELECT r1_0.id, ...
FROM reserva r1_0
JOIN paquete_sorpresa p1_0 ON p1_0.id = r1_0.paquete_sorpresa_id
JOIN negocio n1_0 ON n1_0.id = p1_0.negocio_id
WHERE r1_0.cliente_id = ?
  AND r1_0.estado = 'pendiente'
```

### Criteria 1 — Filtrar paquetes por estado y categoría

**Método:** `PaqueteSorpresaRepository.findAll(spec)` con `PaqueteSorpresaEspecificaciones`

Uso: catálogo filtrado por tipo de comida y disponibilidad. Los dos filtros son opcionales: si alguno viene `null`, la Specification lo neutraliza con `cb.conjunction()` en vez de excluir filas.

```sql
-- con ambos filtros: conEstado("recogido").and(deCategoria(2L))
SELECT p1_0.id, ...
FROM paquete_sorpresa p1_0
WHERE p1_0.estado = ?
  AND p1_0.categoria_id = ?

-- solo con estado (categoría en null): conEstado("disponible").and(deCategoria(null))
SELECT p1_0.id, ...
FROM paquete_sorpresa p1_0
WHERE p1_0.estado = ?
  AND 1=1
```

Sin JOIN en ningún caso: Hibernate filtra `categoria_id` directo en la misma tabla al comparar solo el id de la relación. El `AND 1=1` de la segunda consulta es el `cb.conjunction()` del filtro nulo — no restringe nada, es como si ese filtro no existiera.

### Criteria 2 — Filtrar reservas por cliente y estado

**Método:** `ReservaRepository.findAll(spec)` con `ReservaEspecificaciones`

Uso: historial de compras del cliente según estado de recogida. Igual que arriba, ambos filtros son opcionales.

```sql
SELECT r1_0.id, ...
FROM reserva r1_0
WHERE r1_0.cliente_id = ?
  AND r1_0.estado = ?
```

## Problema N+1

**Caso real detectado:** `findByNegocioIdConCategoria(1L)` emitía **5 sentencias**, no 1, a pesar del `JOIN FETCH` sobre `categoria`. La causa no estaba en esa consulta sino en la entidad: `PaqueteSorpresa` tenía dos lados inversos `@OneToOne(mappedBy=...)` hacia `reserva` y `donacion`, que Hibernate no puede hacer perezosos de verdad sin instrumentación de bytecode — cada paquete cargado disparaba 2 consultas adicionales para comprobar si tenía reserva y si tenía donación (1 consulta de paquetes + 2 paquetes × 2 relaciones = 5).

**Antes** (`findByNegocioId`, sin optimizar): 1 consulta por paquetes + 1 por cada categoría.

```sql
-- consulta 1
SELECT ... FROM paquete_sorpresa WHERE negocio_id = 1;
-- consulta 2 (por cada paquete)
SELECT ... FROM categoria WHERE id = ?;
```

**Con `JOIN FETCH` pero sin corregir la entidad** (`findByNegocioIdConCategoria`, estado real capturado en la retroalimentación del Lab 3): 5 sentencias — 1 con el JOIN a categoría + 2 por cada uno de los 2 paquetes del negocio, por los `@OneToOne` inversos.

**Después** (se eliminan los `@OneToOne(mappedBy=...)` de `PaqueteSorpresa` hacia `reserva`/`donacion`): 1 sola consulta.

```sql
SELECT p1_0.id, c1_0.nombre, ...
FROM paquete_sorpresa p1_0
JOIN categoria c1_0 ON c1_0.id = p1_0.categoria_id
WHERE p1_0.negocio_id = 1;
```

Verificado con `PaqueteSorpresaRepositoryIT.noProduceConsultasExtraAlTraerCategoriaConJoinFetch()`, que usa `Statistics.getPrepareStatementCount()` de Hibernate y afirma que da exactamente 1.

## Pruebas de integración (Testcontainers)

| Clase | Tests |
|---|---|
| SavoraApplicationTests | 1 |
| ClienteRepositoryIT | 2 |
| PaqueteSorpresaRepositoryIT | 5 |
| ReservaRepositoryIT | 2 |
| DonacionRepositoryIT | 1 |
| **Total** | **11** |