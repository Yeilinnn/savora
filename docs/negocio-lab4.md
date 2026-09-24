# Capa de negocio — Laboratorio 4

EIF509 · Savora · II Ciclo 2026

## Procesos de negocio

### Proceso 1 — Reservar un paquete sorpresa (transaccional, con prueba de rollback)

**Servicio:** `ReservaService.reservar(CrearReservaRequest)`

Reglas: el paquete debe existir y estar en estado `disponible`; debe reservarse antes de su hora límite de recogida; el cliente debe existir y no puede exceder su límite de reservas activas (calculado por `PerfilImpactoService.calcularLimiteReservas`, ya implementado en el Lab 2/3: 3 por defecto, 5 si el cliente es "confiable").

Cálculo: el porcentaje de descuento aplicado se calcula a partir de `precioOriginal` y `precioConDescuento` del paquete.

Escrituras atómicas: cambiar el estado del paquete a `reservado` y crear la `Reserva` ocurren en la misma transacción (`@Transactional`). Probado con `ReservaServiceRollbackIT`: se simula una condición de carrera (otro cliente reservó el mismo paquete justo antes) que hace fallar la segunda escritura con una violación de restricción única; se verifica que el cambio de estado del paquete tampoco quedó escrito.

Excepciones propias: `RecursoNoEncontradoException`, `HoraLimiteSuperadaException`, `LimiteReservasActivasException`, `TransicionEstadoInvalidaException` (del patrón State).

### Proceso 2 — Cierre de paquetes no reservados

**Servicio:** `CierreDePaquetesService.cerrar(CerrarPaqueteRequest)`

Reglas: se busca la organización comunitaria con la MENOR capacidad de recolección que aún así alcance para el paquete (`capacidadRecoleccion >= cantidad`) — así no se gasta la capacidad de una organización grande en un paquete pequeño. Si ninguna alcanza, el paquete se marca `perdido`. Si el paquete ya no está `disponible` (p. ej. alguien lo reservó justo antes de este cierre automático), la transición de estado falla y aborta el cierre — esa es la validación de condición de carrera que pide la propuesta de dominio.

Cálculo: la elección de "mejor ajuste" entre las organizaciones candidatas (`min` por capacidad entre las que alcanzan).

Este proceso es intencionalmente independiente del Proceso 3 (historial de impacto en MongoDB) — no llama a `PerfilImpactoService` para escribir nada. Motivo: `PerfilImpactoService` escribe en MongoDB, que no participa de la transacción `@Transactional` de PostgreSQL de este proceso; si dentro de esta misma llamada se incrementara un contador en Mongo y luego la transacción de Postgres tuviera que revertirse, el contador en Mongo quedaría mal (un evento que nunca ocurrió realmente quedaría contado). La propuesta de dominio ya señala esto: "Proceso 3 vive completo en MongoDB — no participa en las transacciones de PostgreSQL del Proceso 1 ni del Proceso 2".

## Patrones de diseño aplicados

### 1 · Specification (ya implementado en el Lab 3)

**Señal:** los métodos de repositorio se multiplicaban por cada combinación de filtros.

**Patrón:** `PaqueteSorpresaEspecificaciones` y `ReservaEspecificaciones` (Evans, DDD cap. 9) construyen predicados combinables y opcionales — un mismo `findAll(spec)` cubre cualquier combinación de filtros sin multiplicar métodos.

### 2 · State (nuevo en el Lab 4)

**Señal:** el estado del paquete (`disponible`, `reservado`, `recogido`, `donado`, `perdido`, `agotado`) tiene transiciones válidas específicas, y esa misma regla se necesitaba en dos procesos distintos (Reservar y Cierre).

**Patrón:** `EstadoPaquete` (enum) declara el mapa de transiciones válidas UNA sola vez; `transitarA(destino)` rechaza cualquier transición no permitida con `TransicionEstadoInvalidaException`. Ambos servicios llaman a `transitarA` en vez de comparar strings a mano.

## Frontera DTO

Records de entrada (`CrearReservaRequest`, `CerrarPaqueteRequest`) con `@NotNull` (Bean Validation) y de salida (`ReservaResumen`, `CierreResumen`). Ninguna entidad JPA se devuelve desde los servicios. Mapeo manual (pocos campos, no justifica MapStruct todavía).

## Pruebas

| Clase | Tipo | Tests |
|---|---|---|
| EstadoPaqueteTest | Unitaria | 4 |
| ReservaServiceTest | Unitaria (Mockito) | 8 |
| CierreDePaquetesServiceTest | Unitaria (Mockito) | 5 |
| ReservaServiceRollbackIT | Integración (Testcontainers) | 1 |
| **Total nuevas** | | **18** |

### Matriz regla de negocio → prueba Mockito

| Regla | Excepción o resultado | Prueba |
|---|---|---|
| Paquete y cliente existen | — | `ReservaServiceTest.reservaExitosaCuandoTodoEstaEnRegla` |
| Descuento desde precios del paquete | `ReservaResumen.descuentoPorcentaje` | `ReservaServiceTest.calculaElPorcentajeDeDescuentoCorrectamente` |
| Paquete inexistente | `RecursoNoEncontradoException` | `ReservaServiceTest.lanzaExcepcionSiElPaqueteNoExiste` |
| Cliente inexistente | `RecursoNoEncontradoException` | `ReservaServiceTest.lanzaExcepcionSiElClienteNoExiste` |
| Hora límite ya pasó | `HoraLimiteSuperadaException` | `ReservaServiceTest.lanzaExcepcionSiYaPasoLaHoraLimite` |
| Límite de reservas activas | `LimiteReservasActivasException` | `ReservaServiceTest.lanzaExcepcionSiElClienteYaAlcanzoSuLimiteDeReservasActivas` |
| Cliente confiable (límite 5) | reserva permitida | `ReservaServiceTest.unClienteConfiablePuedeReservarPorEncimaDelLimiteBase` |
| Paquete no `disponible` al reservar | `TransicionEstadoInvalidaException` | `ReservaServiceTest.lanzaExcepcionSiElPaqueteYaNoEstaDisponible` |
| Donación a org. con menor capacidad que alcanza | `CierreResumen` DONADO | `CierreDePaquetesServiceTest.donaElPaqueteALaOrganizacionConMenorCapacidadQueAunAsiAlcanza` |
| Sin org. con capacidad suficiente | estado `perdido` | `CierreDePaquetesServiceTest.marcaComoPerdidoSiNingunaOrganizacionAlcanza` |
| Paquete inexistente al cerrar | `RecursoNoEncontradoException` | `CierreDePaquetesServiceTest.lanzaExcepcionSiElPaqueteNoExiste` |
| Carrera: paquete ya reservado | `TransicionEstadoInvalidaException` | `CierreDePaquetesServiceTest.lanzaExcepcionSiElPaqueteYaFueReservadoJustoAntesDelCierre` |
| Transición inválida al marcar perdido | `TransicionEstadoInvalidaException` | `CierreDePaquetesServiceTest.siNingunaOrganizacionAlcanzaYElPaqueteYaNoEstaDisponibleTambienLanzaExcepcion` |
| State: `disponible` → `reservado` | — | `EstadoPaqueteTest.disponiblePuedeTransitarAReservado` |
| State: no revertir a `disponible` | `TransicionEstadoInvalidaException` | `EstadoPaqueteTest.reservadoNoPuedeVolverADisponible` |

Rollback transaccional (integración, no Mockito): `ReservaServiceRollbackIT.siLaReservaFallaAMitadDeCaminoNadaQuedaEscrito`.

## Cobertura JaCoCo

- Reporte HTML: `./gradlew build` (genera `build/reports/jacoco/test/html/index.html` al finalizar las pruebas).
- Umbral: **70%** de instrucciones cubiertas en paquetes `cr.ac.una.savora.business` y subpaquetes, aplicado con la tarea `jacocoTestCoverageVerification`.
- **CI:** la tarea `check` (incluida en `./gradlew build`) depende de `jacocoTestCoverageVerification`, así que GitHub Actions falla si la cobertura de negocio baja del mínimo.

Los procesos del Lab 4 se invocan desde la capa de servicio; la capa REST de reservas/cierre puede añadirse en un laboratorio posterior sin cambiar estas reglas.