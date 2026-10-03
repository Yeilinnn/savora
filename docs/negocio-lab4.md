# Capa de negocio — Laboratorio 4

EIF509 · Savora · II Ciclo 2026

## Procesos de negocio

### Proceso 1 — Reservar un paquete sorpresa (transaccional, con prueba de rollback)

**Servicio:** `ReservaService.reservar(CrearReservaRequest)`

Reglas: el paquete debe existir y estar en estado `disponible`; debe reservarse antes de su hora límite de recogida; el cliente debe existir y no puede exceder su límite de reservas activas (calculado por `PerfilImpactoService.calcularLimiteReservas`, ya implementado en el Lab 2/3: 3 por defecto, 5 si el cliente es "confiable").

Cálculo: el porcentaje de descuento aplicado se calcula a partir de `precioOriginal` y `precioConDescuento` del paquete.

Stock: se valida `cantidad > 0`, se descuenta una unidad y, si queda en cero, la transición va a `agotado`; si aún hay stock, a `reservado`.

Escrituras atómicas: actualizar el paquete (`saveAndFlush`) y crear la `Reserva` ocurren en la misma transacción (`@Transactional`). `ReservaServiceRollbackIT` fuerza el fallo en el INSERT de la reserva **después** de haber persistido el UPDATE del paquete; el rollback debe dejar estado y cantidad como antes.

Excepciones propias: `RecursoNoEncontradoException`, `HoraLimiteSuperadaException`, `LimiteReservasActivasException`, `TransicionEstadoInvalidaException` (del patrón State).

### Proceso 2 — Cierre de paquetes no reservados

**Servicio:** `CierreDePaquetesService.cerrar(CerrarPaqueteRequest)`

Reglas: solo se cierra si **ya pasó** la hora límite (`CierreAntesDeHoraLimiteException` si no). Se recorren organizaciones con capacidad suficiente (menor capacidad primero); la elegida genera donación `aceptada` y las demás elegibles quedan en `organizacionesRechazadas` del resumen. Si ninguna alcanza, el paquete se marca `perdido`. Si el paquete ya no está `disponible`, la transición de estado aborta el cierre (condición de carrera).

Cálculo: kilogramos donados en este cierre (`cantidad` del paquete) y acumulado del negocio sumando paquetes en estado `donado` (Specification + repositorio; el impacto en MongoDB sigue en el Proceso 3).

`cerrarPaquetesVencidosDisponibles()` compone **Specification** (`disponible` + hora límite vencida) para batch de cierres.

Este proceso es intencionalmente independiente del Proceso 3 (historial de impacto en MongoDB) — no llama a `PerfilImpactoService` para escribir nada. Motivo: `PerfilImpactoService` escribe en MongoDB, que no participa de la transacción `@Transactional` de PostgreSQL de este proceso; si dentro de esta misma llamada se incrementara un contador en Mongo y luego la transacción de Postgres tuviera que revertirse, el contador en Mongo quedaría mal (un evento que nunca ocurrió realmente quedaría contado). La propuesta de dominio ya señala esto: "Proceso 3 vive completo en MongoDB — no participa en las transacciones de PostgreSQL del Proceso 1 ni del Proceso 2".

## Patrones de diseño aplicados

### 1 · Specification (Lab 3, usado en Lab 4)

**Señal:** los métodos de repositorio se multiplicaban por cada combinación de filtros.

**Patrón:** `PaqueteSorpresaEspecificaciones` y `ReservaEspecificaciones` (Evans, DDD cap. 9). En este laboratorio, `CierreDePaquetesService.cerrarPaquetesVencidosDisponibles()` y el acumulado de kg donados por negocio componen specs (`conEstado`, `horaLimiteVencida`, `deNegocio`) con `findAll(spec)`.

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