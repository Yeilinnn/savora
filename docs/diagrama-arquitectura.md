# Diagrama de Arquitectura

Laboratorio 1 y 2 · Savora

## Capas del sistema

```mermaid
flowchart TB
    Cliente["Cliente (navegador)"]

    subgraph presentation ["Presentation"]
        SaludController
        CategoriaController
    end

    subgraph business ["Business"]
        CategoriaService
    end

    subgraph data ["Data"]
        CategoriaRepository
    end

    subgraph config ["Config"]
        ConfigVacio["(vacío por ahora)"]
    end

    BD["Base de datos"]

    Cliente --> SaludController
    Cliente --> CategoriaController
    CategoriaController --> CategoriaService
    CategoriaService --> CategoriaRepository
    CategoriaRepository --> BD
```

Las dependencias van en una sola dirección: **presentation → business → data**. Un controlador puede llamar a un servicio; un servicio no conoce la capa de presentación.

`PerfilImpactoController`, `PerfilImpactoService` y `PerfilImpactoRepository` siguen las mismas capas que `CategoriaController`/`CategoriaService`/`CategoriaRepository`, solo que `PerfilImpactoRepository` es un `MongoRepository` en vez de una implementación manual.

## Decisión: PostgreSQL vs MongoDB

Se aplican las 3 preguntas de diseño de la Sesión 4 para decidir en qué base vive cada dato:

```mermaid
flowchart TD
    A["¿Qué dato estamos modelando?"] --> B{"¿Cómo se lee<br/>el 90% del tiempo?"}
    B -->|"Siempre junto al perfil<br/>de su dueño"| C{"¿Cuánto crece<br/>en el peor caso?"}
    B -->|"Independiente, se consulta<br/>suelto o se cruza con otras tablas"| G["PostgreSQL"]
    C -->|"Sin límite / estructura<br/>variable"| D{"¿Quién más<br/>lo necesita?"}
    C -->|"Tamaño acotado,<br/>estructura fija"| G
    D -->|"Solo el dueño del dato<br/>lo consulta"| E["MongoDB — incrustar"]
    D -->|"Lo comparten varias entidades<br/>o exige transacción con otras tablas"| G

    G --> H["Negocio, Categoria, Cliente,<br/>OrganizacionComunitaria,<br/>PaqueteSorpresa, Reserva"]
    E --> F["PerfilImpacto<br/>(insignias incrustadas)"]
```

**Resultado:**

| Aspecto | Dónde vive | Por qué |
|---|---|---|
| Negocio, Cliente, Categoria, OrganizacionComunitaria | PostgreSQL | Datos maestros, se cruzan en joins, estructura fija |
| PaqueteSorpresa, Reserva | PostgreSQL | Exigen consistencia transaccional: stock, estados, FK, UNIQUE |
| PerfilImpacto (kg rescatados, ratios, insignias) | MongoDB | Se lee siempre completo junto a su dueño, crece sin límite, sin joins ni transacciones |
| Resena (calificación, comentario) | MongoDB | Se lee siempre junto al paquete/negocio, estructura variable, sin límite de cantidad |

## Modelo de dominio

```mermaid
erDiagram
    Negocio ||--o{ PaqueteSorpresa : publica
    Categoria ||--o{ PaqueteSorpresa : clasifica
    Cliente ||--o{ Reserva : realiza
    PaqueteSorpresa ||--o| Reserva : "tiene como máximo una"
    OrganizacionComunitaria ||--o{ PaqueteSorpresa : "puede recibir donación"
    Cliente ||--o| PerfilImpacto : "referencia por id (Mongo, no es FK real)"
    Negocio ||--o| PerfilImpacto : "referencia por id (Mongo, no es FK real)"
    Cliente ||--o{ Resena : deja
    PaqueteSorpresa ||--o{ Resena : recibe

    Negocio {
        string nombre
        string tipo
        string ubicacion
        string horarioCierre
    }

    Cliente {
        string nombre
        string correo
        string telefono
    }

    PaqueteSorpresa {
        string descripcion
        int cantidad
        decimal precioOriginal
        decimal precioConDescuento
        datetime horaLimiteRecogida
        string estado
    }

    Reserva {
        datetime fechaHora
        string estado
    }

    Categoria {
        string nombre
    }

    OrganizacionComunitaria {
        string nombre
        string tipo
        int capacidadRecoleccion
        string contacto
    }

    PerfilImpacto {
        string propietarioId "id de Cliente o Negocio, referencia lógica"
        string tipoPropietario "CLIENTE o NEGOCIO"
        decimal kgRescatados
        int reservasRecogidas
        int reservasNoRecogidas
        int paquetesDonados
        int paquetesPerdidos
        array insignias "incrustadas: codigo, fechaObtenida"
    }

    Resena {
        int calificacion "1 a 5"
        string comentario
        datetime fecha
    }
```

### Reglas del dominio

- Correo del cliente único.
- Precio con descuento menor al precio original.
- Máximo N reservas activas por cliente (el límite base sube si el cliente tiene buena reputación en `PerfilImpacto`).
- Un negocio con buen ratio de `paquetesDonados`/`paquetesPerdidos` gana mejor posicionamiento en el catálogo.
- El historial de compras del cliente se obtiene consultando `Reserva` por `clienteId` — no es un campo propio de `Cliente`.