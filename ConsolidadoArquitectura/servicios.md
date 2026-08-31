# Capa de servicios y mapeo a historias de usuario

## Principios

- Cada servicio recibe `empresaId` del controlador (leido de HttpSession).
- Las validaciones de negocio van en el servicio, antes de persistir.
- Cada operacion de escritura registra un `HistorialCambio`.
- Si una regla falla, se lanza `ReglaNegocioException` (RuntimeException).

## Servicios del bloque gestion

### EmpresaService

| Metodo | HU | Que hace |
|--------|----|----------|
| registrar(nombre, nit, correo, passwordAdmin) | HU-01 | Crea empresa + usuario admin inicial. Valida NIT unico. |
| buscarPorNit(nit) | HU-01 | Busca empresa por NIT. |

### UsuarioService

| Metodo | HU | Que hace |
|--------|----|----------|
| crearColaborador(empresaId, nombre, email, password, rolAcceso) | HU-02 | Crea usuario. Valida email unico en la empresa. |
| cambiarRol(empresaId, usuarioId, nuevoRol) | HU-02 | Cambia el rolAcceso del usuario. |
| desactivar(empresaId, usuarioId) | HU-02 | Pone activo=false. Preserva historial. |
| autenticar(email, password) | HU-03 | Valida credenciales, retorna usuario o error generico. |
| listarPorEmpresa(empresaId) | HU-02 | Lista usuarios activos de la empresa. |

### ProcesoService

| Metodo | HU | Que hace |
|--------|----|----------|
| crear(empresaId, usuarioId, nombre, descripcion, categoria) | HU-04 | Crea proceso en BORRADOR. Valida nombre unico. Crea pool inicial de la empresa. |
| editar(empresaId, procesoId, nombre, descripcion, categoria) | HU-05 | Actualiza datos. Solo admin/editor. |
| publicar(empresaId, procesoId) | HU-05 | Cambia estado a PUBLICADO. |
| eliminarLogico(empresaId, procesoId) | HU-06 | Pone activo=false. Solo admin. |
| buscar(empresaId, nombre, estado, categoria, pagina) | HU-07 | Lista paginada con filtros. |
| obtener(empresaId, procesoId) | HU-07 | Detalle de un proceso con su historial. |

### HistorialCambioService

| Metodo | HU | Que hace |
|--------|----|----------|
| registrar(procesoId, usuarioId, descripcion) | Todas | Inserta registro. Solo INSERT. |
| listarPorProceso(procesoId) | HU-07 | Lista historial ordenado por fecha descendente. |

### RolProcesoService

| Metodo | HU | Que hace |
|--------|----|----------|
| crear(empresaId, nombre, descripcion) | HU-17 | Crea rol. Valida nombre unico en empresa. Solo admin. |
| editar(empresaId, rolId, nombre, descripcion) | HU-18 | Actualiza datos del rol. |
| eliminar(empresaId, rolId) | HU-19 | Elimina. Valida que ninguna Lane lo use. |
| listarPorEmpresa(empresaId) | HU-20 | Lista roles disponibles. |

## Servicios del bloque modelado

### PoolService

| Metodo | HU | Que hace |
|--------|----|----------|
| crear(empresaId, procesoId, nombre, tipoParticipante, cajaNegra) | HU-21 | Crea pool en el proceso. |
| editar(empresaId, poolId, nombre, tipoParticipante) | HU-23 | Actualiza datos del pool. |
| eliminar(empresaId, poolId) | HU-23 | Elimina pool. Valida que no tenga lanes con actividades. |
| listarPorProceso(empresaId, procesoId) | HU-21 | Lista pools del proceso. |

### LaneService

| Metodo | HU | Que hace |
|--------|----|----------|
| crear(empresaId, poolId, nombre, rolProcesoId) | HU-22 | Crea lane asociada a un rol. |
| editar(empresaId, laneId, nombre, rolProcesoId) | HU-22 | Actualiza lane. |
| eliminar(empresaId, laneId) | HU-24 | Elimina. Valida que no tenga actividades. |
| listarPorPool(empresaId, poolId) | HU-22 | Lista lanes del pool. |

### ActividadService

| Metodo | HU | Que hace |
|--------|----|----------|
| crear(empresaId, laneId, nombre, descripcion, posX, posY) | HU-08 | Crea actividad. Valida nombre unico en proceso. |
| editar(empresaId, actividadId, nombre, descripcion, posX, posY) | HU-09 | Actualiza datos y posicion. |
| eliminar(empresaId, actividadId) | HU-10 | Elimina actividad y sus arcos asociados. |

### GatewayService

| Metodo | HU | Que hace |
|--------|----|----------|
| crear(empresaId, laneId, nombre, tipoGateway, posX, posY) | HU-14 | Crea gateway. |
| editar(empresaId, gatewayId, nombre, tipoGateway, posX, posY) | HU-15 | Actualiza. |
| eliminar(empresaId, gatewayId) | HU-16 | Elimina gateway y sus arcos asociados. |

### ArcoService

| Metodo | HU | Que hace |
|--------|----|----------|
| crear(empresaId, origenId, destinoId, etiqueta, condicion) | HU-11 | Crea arco. Valida: mismo pool, no auto-referencia, no duplicado. |
| editar(empresaId, arcoId, etiqueta, condicion) | HU-12 | Actualiza etiqueta/condicion. |
| eliminar(empresaId, arcoId) | HU-13 | Elimina arco. |

Validaciones en `crear`:
- Origen != destino (regla 7).
- Mismo pool (regla 8).
- No duplicado (regla 9).
- Si destino es gateway exclusivo/inclusivo, condicion obligatoria (regla 10).

### MensajeService

| Metodo | HU | Que hace |
|--------|----|----------|
| crear(empresaId, procesoId, nombre, contenido, poolOrigenId, poolDestinoId) | HU-25, HU-26, HU-27 | Crea mensaje. Valida pools distintos. |
| editar(empresaId, mensajeId, nombre, contenido) | HU-25 | Actualiza datos. |
| eliminar(empresaId, mensajeId) | HU-25 | Elimina mensaje y su correlacion. |
| listarPorProceso(empresaId, procesoId) | HU-25 | Lista mensajes del proceso. |

El campo `contenido` documenta los datos enviados y su tipo (HU-25 criterio 2, HU-26 criterio 3). El pool destino con `tipoParticipante=SISTEMA_EXTERNO` y `cajaNegra=true` cubre HU-26.

### CorrelacionService

| Metodo | HU | Que hace |
|--------|----|----------|
| definir(empresaId, mensajeId, criterio) | HU-28 | Crea o actualiza la correlacion del mensaje. |
| obtener(empresaId, mensajeId) | HU-28 | Retorna la correlacion. |

## Mapeo completo HU → Entidad → Servicio

| HU | Titulo | Entidad principal | Servicio |
|----|--------|-------------------|----------|
| HU-01 | Registro de empresa | Empresa, Usuario | EmpresaService |
| HU-02 | Registro de usuario | Usuario | UsuarioService |
| HU-03 | Inicio de sesion | Usuario | UsuarioService |
| HU-04 | Crear proceso | Proceso, Pool | ProcesoService |
| HU-05 | Editar proceso | Proceso | ProcesoService |
| HU-06 | Eliminar proceso | Proceso | ProcesoService |
| HU-07 | Consultar procesos | Proceso, HistorialCambio | ProcesoService |
| HU-08 | Crear actividad | Actividad | ActividadService |
| HU-09 | Editar actividad | Actividad | ActividadService |
| HU-10 | Eliminar actividad | Actividad | ActividadService |
| HU-11 | Crear arco | Arco | ArcoService |
| HU-12 | Editar arco | Arco | ArcoService |
| HU-13 | Eliminar arco | Arco | ArcoService |
| HU-14 | Crear gateway | Gateway | GatewayService |
| HU-15 | Editar gateway | Gateway | GatewayService |
| HU-16 | Eliminar gateway | Gateway | GatewayService |
| HU-17 | Crear rol de proceso | RolProceso | RolProcesoService |
| HU-18 | Editar rol de proceso | RolProceso | RolProcesoService |
| HU-19 | Eliminar rol de proceso | RolProceso | RolProcesoService |
| HU-20 | Consultar roles | RolProceso | RolProcesoService |
| HU-21 | Configurar pool | Pool | PoolService |
| HU-22 | Configurar lanes | Lane | LaneService |
| HU-23 | Editar pool | Pool | PoolService |
| HU-24 | Eliminar lane | Lane | LaneService |
| HU-25 | Enviar mensaje (throw) | Mensaje | MensajeService |
| HU-26 | Mensaje a sistema externo | Mensaje, Pool | MensajeService |
| HU-27 | Recibir mensaje (catch) | Mensaje | MensajeService |
| HU-28 | Correlacion de mensajes | Correlacion | CorrelacionService |
