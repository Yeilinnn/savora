# ADR-003 · SQL o NoSQL para cada subdominio

**Estado y fecha:** aceptada (agosto de 2026) · **Responsables:** Yeilin Moya Baltodano, Nazareth Gómez Gómez

## Contexto

El sistema mezcla datos con relaciones y consistencia estricta (paquetes, reservas, stock, pagos) con datos que se
leen siempre junto a su dueño, crecen sin límite y cambian de forma (historial de impacto con insignias, reseñas).
Se aplicaron las tres preguntas de diseño de la Sesión 4: cómo se lee el dato, cuánto crece y quién más lo necesita.

## Decisión

Persistencia políglota:

- **PostgreSQL** (migraciones Flyway) para negocios, categorías, clientes, organizaciones, paquetes, reservas,
  donaciones, usuarios y pagos.
- **MongoDB** para `PerfilImpacto` (contadores, racha semanal e insignias incrustadas) y `Resena`.

El vínculo entre ambos mundos es por id (`propietarioId`), sin llave foránea real.

## Alternativas consideradas

1. **Todo en PostgreSQL:** se descartó para el perfil de impacto. Las insignias serían una tabla más con joins para
   algo que siempre se lee completo junto a su dueño, y cada insignia nueva exigiría una migración.
2. **Todo en MongoDB:** se descartó porque reservas y stock necesitan transacciones, llaves foráneas y restricciones
   únicas (`uq_reserva_paquete`) y el bloqueo optimista con `@Version`.
3. **Redis u otro almacén clave-valor para el impacto:** se descartó por añadir una tercera tecnología para un
   problema que MongoDB ya resuelve bien con documentos.

## Consecuencias

- **Positivas:** cada dato vive donde mejor encaja; agregar una insignia nueva no requiere migrar nada.
- **Negativas:** MongoDB no participa en las transacciones de PostgreSQL. Si el commit falla después de actualizar
  el perfil, el contador puede quedar adelantado (consistencia eventual). Se aceptó porque el perfil es
  informativo y no mueve dinero ni stock.
- **Neutras:** hay dos bases que operar, dos cadenas de conexión (`DB_URL` y `MONGODB_URI`) y dos seeders.