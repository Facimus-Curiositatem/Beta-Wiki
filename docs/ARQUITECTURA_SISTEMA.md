# Arquitectura visual de Beta

## Vista general

```mermaid
flowchart LR
    C[Cliente / Frontend] -->|HTTP JSON| API[/API REST /api/v1/]
    API --> SEC[JWT + SecurityConfig]
    SEC --> CTRL[Controllers]
    CTRL --> SRV[Services]
    SRV --> REP[Repositories JPA]
    REP --> DB[(H2 / PostgreSQL)]
    SRV --> AUD[Auditoria / Historial]
```

## Capas

- **Security** valida JWT, usuario activo, tenant y rol.
- **Controller** recibe HTTP/JSON y devuelve DTOs.
- **Service** aplica reglas de negocio y transacciones.
- **Repository** encapsula consultas Spring Data JPA.
- **Model** representa gestión empresarial y BPMN.

## Flujo autenticado

```mermaid
sequenceDiagram
    participant U as Cliente
    participant F as JwtAuthenticationFilter
    participant C as Controller
    participant S as Service
    participant R as Repository
    participant D as DB

    U->>F: Request + Bearer JWT
    F->>F: Validar token y usuario
    F->>C: ApiPrincipal
    C->>S: empresaId + datos
    S->>R: consulta tenant-aware
    R->>D: SQL/JPA
    D-->>R: datos
    R-->>S: entidades
    S-->>C: resultado
    C-->>U: Response DTO JSON
```

## Módulos principales

- `common/`: excepciones, entidad tenant y paginación.
- `config/`: OpenAPI e inicialización.
- `security/`: JWT, CORS y autorización.
- `gestion/`: empresas, usuarios, procesos, roles e historial.
- `modelado/`: pools, lanes, nodos, arcos y mensajes.
