# Persistencia — Laboratorio 3

EIF509 · Savora · II Ciclo 2026

## Entidades JPA

| Entidad PostgreSQL | Clase JPA | Relaciones LAZY |
|---|---|---|
| categoria | Categoria | — |
| negocio | Negocio | — |
| cliente | Cliente | — |
| organizacion_comunitaria | OrganizacionComunitaria | — |
| paquete_sorpresa | PaqueteSorpresa | negocio, categoria, reserva, donacion |
| reserva | Reserva | cliente, paqueteSorpresa |
| donacion | Donacion | paqueteSorpresa, organizacionComunitaria |
| perfil_impacto | PerfilImpacto | MongoDB (PerfilImpactoRepository) |

Configuración: `ddl-auto=validate` contra migraciones Flyway V1–V5.

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

Uso: catálogo filtrado por tipo de comida y disponibilidad.

```sql
SELECT p1_0.id, ...
FROM paquete_sorpresa p1_0
JOIN categoria c1_0 ON c1_0.id = p1_0.categoria_id
WHERE p1_0.estado = ?
  AND c1_0.id = ?
```

### Criteria 2 — Filtrar reservas por cliente y estado

**Método:** `ReservaRepository.findAll(spec)` con `ReservaEspecificaciones`

Uso: historial de compras del cliente según estado de recogida.

```sql
SELECT r1_0.id, ...
FROM reserva r1_0
WHERE r1_0.cliente_id = ?
  AND r1_0.estado = ?
```

## Problema N+1

**Caso:** listar paquetes de un negocio y mostrar la categoría de cada uno.

**Antes** (`findByNegocioId`): 1 consulta por paquetes + 1 por cada categoría.

```sql
-- consulta 1
SELECT ... FROM paquete_sorpresa WHERE negocio_id = 1;
-- consulta 2 (por cada paquete)
SELECT ... FROM categoria WHERE id = ?;
```

**Después** (`findByNegocioIdConCategoria` con JOIN FETCH): 1 sola consulta.

```sql
SELECT p1_0.id, c1_0.nombre, ...
FROM paquete_sorpresa p1_0
JOIN categoria c1_0 ON c1_0.id = p1_0.categoria_id
WHERE p1_0.negocio_id = 1;
```

## Pruebas de integración (Testcontainers)

| Clase | Tests |
|---|---|
| SavoraApplicationTests | 1 |
| ClienteRepositoryIT | 2 |
| PaqueteSorpresaRepositoryIT | 3 |
| ReservaRepositoryIT | 2 |
| DonacionRepositoryIT | 1 |
| **Total** | **9** |
