# ADR-001 · Versión de Spring Boot para el proyecto

**Estado:** Aceptada · **Fecha:** 08/08/2026 · **Responsables:** Yeilin Moya Baltodano, Nazareth Gómez Gómez

## Contexto

El stack de referencia del curso EIF509 recomienda Java 21 + Spring Boot 3 + Gradle. Al generar el proyecto en start.spring.io siguiendo esa recomendación, encontramos que la rama 3 de Spring Boot ya no está disponible como opción en el generador: las únicas versiones que ofrece actualmente son de la rama 4 (4.0.7, 4.0.8-SNAPSHOT, 4.1.0, 4.1.1-SNAPSHOT). No confirmamos la razón exacta por la que el generador dejó de ofrecer la rama 3, pero el hecho es que ya no aparece como opción. Necesitábamos decidir cómo proceder sin poder cumplir literalmente la versión sugerida por el curso, sin forzar una versión que la herramienta oficial ya no lista.

## Decisión

Usaremos **Spring Boot 4.1.0** como versión del framework, manteniéndonos dentro del criterio original del curso ("la versión estable más alta que no diga SNAPSHOT ni M"), pero en la rama 4 en lugar de la 3, ya que esta última no está disponible.

## Alternativas consideradas

1. **Generar el proyecto igual y editar manualmente `build.gradle` para forzar Spring Boot 3.3.x:** se descartó porque el generador oficial (start.spring.io) ya no la ofrece como opción, y forzar una versión que la propia herramienta del ecosistema Spring dejó de listar introduce un riesgo innecesario: no hay garantía de que todas las dependencias sigan siendo totalmente compatibles entre sí a esta fecha.
2. **Usar un stack completamente distinto (Node.js + Nest.js), ya que el equipo tiene experiencia previa con ese ecosistema:** se descartó porque el curso está diseñado alrededor de Spring (inyección de dependencias, JPA, Actuator), y cambiar de stack requeriría solicitud formal y adaptar todo el material de referencia del curso a un ecosistema distinto, lo cual añade riesgo y trabajo extra sin necesidad, dado que la única traba real era la versión, no el framework en sí.

## Consecuencias

- **Positivas:** seguimos usando el ecosistema Spring que el curso enseña, sin necesidad de pedir una excepción de stack; usamos la versión que el propio generador oficial ofrece activamente, en vez de forzar una versión que ya no está disponible ahí.
- **Negativas:** la documentación y los ejemplos del curso están pensados para Spring Boot 3, por lo que podríamos encontrar pequeñas diferencias de sintaxis o de dependencias que requieran ajustes menores al seguir las guías.
- **Neutras:** esta decisión no afecta la organización por capas del proyecto ni el resto del stack (Java 21, Gradle); solo cambia la versión puntual del framework.

## Referencias

- Guía de configuración del ambiente, Laboratorio 1, EIF509.
- start.spring.io (verificado el 08/08/2026: rama 3 no disponible).
