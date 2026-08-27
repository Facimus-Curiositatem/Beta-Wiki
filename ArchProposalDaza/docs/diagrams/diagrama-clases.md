# Diagrama de clases — modelo de dominio

Generado a partir de las 13 entidades JPA en `src/main/java/com/facimus/procesos/`.

```mermaid
classDiagram
    class EntidadEmpresa {
        <<MappedSuperclass>>
        Long id
        Empresa empresa
    }

    class Empresa {
        Long id
        String nombre
        String nit
        LocalDateTime fechaRegistro
    }

    class Usuario {
        String nombre
        String email
        String passwordHash
        RolAcceso rolAcceso
    }

    class Proceso {
        String nombre
        String descripcion
        String categoria
        EstadoProceso estado
        boolean activo
        LocalDateTime fechaCreacion
        LocalDateTime fechaModificacion
    }

    class HistorialCambio {
        LocalDateTime fechaCambio
        String descripcionCambio
    }

    class RolProceso {
        String nombre
    }

    class Pool {
        String nombre
        TipoParticipante tipoParticipante
    }

    class Lane {
        String nombre
    }

    class NodoFlujo {
        <<abstract>>
        String nombre
    }

    class Actividad {
        String descripcion
    }

    class Gateway {
        TipoGateway tipoGateway
    }

    class Arco {
        String etiqueta
    }

    class Mensaje {
        String nombre
    }

    class Correlacion {
        String criterio
    }

    class RolAcceso {
        <<enumeration>>
        ADMINISTRADOR
        EDITOR
        LECTURA
    }

    class EstadoProceso {
        <<enumeration>>
        BORRADOR
        PUBLICADO
    }

    class TipoParticipante {
        <<enumeration>>
        EMPRESA
        CLIENTE
        PROVEEDOR
        SISTEMA_EXTERNO
    }

    class TipoGateway {
        <<enumeration>>
        EXCLUSIVA
        PARALELA
        INCLUSIVA
    }

    EntidadEmpresa <|-- Usuario
    EntidadEmpresa <|-- Proceso
    EntidadEmpresa <|-- HistorialCambio
    EntidadEmpresa <|-- RolProceso
    EntidadEmpresa <|-- Pool
    EntidadEmpresa <|-- Lane
    EntidadEmpresa <|-- NodoFlujo
    EntidadEmpresa <|-- Arco
    EntidadEmpresa <|-- Mensaje
    EntidadEmpresa <|-- Correlacion
    NodoFlujo <|-- Actividad
    NodoFlujo <|-- Gateway

    EntidadEmpresa "*" --> "1" Empresa : empresa_id

    Empresa "1" --> "*" Usuario
    Empresa "1" --> "*" Proceso

    Proceso "1" --> "*" RolProceso
    Proceso "1" --> "*" Pool
    Proceso "1" --> "*" HistorialCambio
    HistorialCambio "*" --> "1" Usuario : autor

    Pool "1" --> "*" Lane
    Lane "*" --> "1" RolProceso
    Lane "1" --> "*" NodoFlujo

    Arco "*" --> "1" NodoFlujo : origen
    Arco "*" --> "1" NodoFlujo : destino

    Mensaje "*" --> "1" Pool : poolOrigen
    Mensaje "*" --> "1" Pool : poolDestino
    Mensaje "1" --> "1" Correlacion

    Usuario --> RolAcceso
    Proceso --> EstadoProceso
    Pool --> TipoParticipante
    Gateway --> TipoGateway
```

## Notas de lectura

- `EntidadEmpresa` no es una entidad propia: es un `@MappedSuperclass` que
  aporta `id` y `empresa` a todas las tablas excepto `Empresa`. Ver
  [ADR-002](../adr/ADR-002-multitenencia-empresa-id.md).
- `NodoFlujo` es `@Inheritance(SINGLE_TABLE)`: `Actividad` y `Gateway` viven
  en la misma tabla `nodo_flujo`, discriminadas por `tipo_nodo`. Así `Arco` y
  `Lane` apuntan a "cualquier nodo" con una sola FK.
- El cruce real entre los paquetes `modelado` y `gestion` es
  `Lane -> RolProceso` (el rol se asigna a nivel de lane, no de actividad).
