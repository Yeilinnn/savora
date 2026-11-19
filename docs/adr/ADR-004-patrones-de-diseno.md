# ADR-004 · Patrones de diseño aplicados

**Estado y fecha:** aceptada (septiembre de 2026) · **Responsables:** Yeilin Moya Baltodano, Nazareth Gómez Gómez

## Contexto

La capa de negocio acumulaba condiciones repetidas: filtros de paquetes y reservas armados a mano, estados
comparados como cadenas de texto en varios servicios y entidades JPA expuestas directamente por la API. Eso volvía
frágil el código y difícil de probar.

## Decisión

Aplicar estos patrones, cada uno donde resuelve un problema concreto:

- **Capas** (`presentation → business → data`): los controladores no contienen reglas; los servicios no conocen HTTP.
- **Repository** (Spring Data JPA y `MongoRepository`) para el acceso a datos.
- **Specification** (`PaqueteSorpresaEspecificaciones`, `ReservaEspecificaciones`): compone filtros
  (`conEstado`, `horaLimiteVencida`, `deNegocio`) sin multiplicar métodos de consulta, y permite `JOIN FETCH`
  para evitar el problema N+1 en los listados.
- **State** (`EstadoPaquete`): el mapa de transiciones válidas vive en un solo lugar y `transitarA` rechaza las
  inválidas con `TransicionEstadoInvalidaException`. Es lo que impide donar un paquete ya reservado.
- **DTO** (records) y un mapeador: la API nunca expone entidades JPA.
- **Inyección de dependencias del reloj** (`Clock`): hace determinista cualquier regla que dependa del tiempo.

## Alternativas consideradas

1. **Métodos de repositorio por cada combinación de filtros:** se descartó por la explosión de métodos.
2. **Comparar estados con cadenas en cada servicio:** se descartó porque una transición inválida pasaba sin error.
3. **Exponer las entidades directamente en la API:** se descartó por el acoplamiento al esquema y el riesgo de
   filtrar campos que no deben salir.

## Consecuencias

- **Positivas:** reglas en un solo lugar, servicios fáciles de probar con Mockito y relojes fijos, listados sin N+1.
- **Negativas:** más clases (DTO, mapeadores, especificaciones) y una curva de aprendizaje para quien llega nuevo.
- **Neutras:** el estado se guarda como texto en la base; el enum solo valida las transiciones en la capa de negocio.