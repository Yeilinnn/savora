# Diagrama de Arquitectura

Laboratorio 1 · Savora

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

## Modelo de dominio

```mermaid
erDiagram
    Negocio ||--o{ PaqueteSorpresa : publica
    Categoria ||--o{ PaqueteSorpresa : clasifica
    Cliente ||--o{ Reserva : realiza
    PaqueteSorpresa ||--o| Reserva : "tiene como máximo una"
    OrganizacionComunitaria ||--o{ PaqueteSorpresa : "puede recibir donación"

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
```

### Reglas del dominio

- Correo del cliente único.
- Precio con descuento menor al precio original.
- Máximo N reservas activas por cliente.
