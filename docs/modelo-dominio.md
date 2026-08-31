# Propuesta aprobada — Modelo de dominio

## Objetivo

Este documento consolida la versión aprobada del diagrama de clases del proyecto de Desarrollo Web.

El modelo representa el dominio completo del sistema y marca con `<<Entrega1>>` las clases que forman parte del primer alcance de implementación.

## Diagrama de clases

```mermaid
classDiagram
direction TB

class Empresa {
    <<Entrega1>>
    +id
    +nombre
    +nit
    +correoContacto
}

class Usuario {
    <<Entrega1>>
    +id
    +correo
    +contrasena
    +activo
}

class Proceso {
    <<Entrega1>>
    +id
    +nombre
    +descripcion
    +categoria
    +estado
    +activo
}

class HistorialCambio {
    <<Entrega1>>
    +id
    +fechaHora
    +accion
    +elementoAfectado
    +descripcion
}

class RolAcceso {
    <<enumeration>>
    ADMINISTRADOR
    EDITOR
    SOLO_LECTURA
}

class EstadoProceso {
    <<enumeration>>
    BORRADOR
    PUBLICADO
}

class AccionHistorial {
    <<enumeration>>
    CREACION
    EDICION
    ELIMINACION
    COMPARTICION
}

Empresa "1" *-- "1..*" Usuario : tiene
Empresa "1" *-- "0..*" Proceso : posee

Usuario --> "1" RolAcceso : tiene
Proceso --> "1" EstadoProceso : estado

Proceso "1" *-- "0..*" HistorialCambio : historial
Usuario "1" --> "0..*" HistorialCambio : realiza
HistorialCambio --> "1" AccionHistorial : accion

class RolProceso {
    +id
    +nombre
    +descripcion
    +activo
}

Empresa "1" *-- "0..*" RolProceso : define

class ComparticionProceso {
    +id
    +fechaComparticion
    +activa
}

Proceso "1" *-- "0..*" ComparticionProceso : comparte
ComparticionProceso "0..*" --> "1" Empresa : empresaInvitada

class Pool {
    +id
    +nombre
    +tipo
    +cajaNegra
    +orden
    +activo
}

class Lane {
    +id
    +nombre
    +orden
    +activo
}

class TipoPool {
    <<enumeration>>
    EMPRESA_PROPIETARIA
    EMPRESA_EXTERNA
    CLIENTE
    PROVEEDOR
    SISTEMA_EXTERNO
}

Proceso "1" *-- "1..*" Pool : contiene
Pool --> "1" TipoPool : tipo

Pool "1" *-- "0..*" Lane : contiene
Lane "0..*" --> "1" RolProceso : responsable

class PermisoPool {
    +id
    +crearPool
    +editarPool
    +eliminarPool
    +crearLane
    +editarLane
    +eliminarLane
}

Pool "1" *-- "0..*" PermisoPool : permisos
PermisoPool --> "1" RolAcceso : aplicaA

class Nodo {
    <<abstract>>
    +id
    +nombre
    +posicionX
    +posicionY
    +activo
}

class Actividad {
    +tipo
}

class Gateway {
    +tipo
}

class EventoMensaje {
    <<abstract>>
}

class MessageThrow

class MessageCatch {
    +tipoRecepcion
    +origenExterno
    +accionSinCaso
}

Nodo <|-- Actividad
Nodo <|-- Gateway
Nodo <|-- EventoMensaje

EventoMensaje <|-- MessageThrow
EventoMensaje <|-- MessageCatch

class TipoGateway {
    <<enumeration>>
    EXCLUSIVO
    PARALELO
    INCLUSIVO
}

class TipoRecepcionMensaje {
    <<enumeration>>
    INICIO
    INTERMEDIO
}

class AccionSinCaso {
    <<enumeration>>
    DESCARTAR
    INICIAR_NUEVO_CASO
}

Gateway --> "1" TipoGateway : tipo
MessageCatch --> "1" TipoRecepcionMensaje : tipo
MessageCatch --> "1" AccionSinCaso : accionSinCaso

Pool "1" *-- "0..*" Nodo : contiene
Lane "1" o-- "0..*" Actividad : contiene

class Arco {
    +id
    +etiqueta
    +condicion
    +activo
}

Pool "1" *-- "0..*" Arco : contiene

Arco "0..*" --> "1" Nodo : origen
Arco "0..*" --> "1" Nodo : destino

class Mensaje {
    +id
    +nombre
    +claveCorrelacion
    +activo
}

class CampoMensaje {
    +id
    +nombre
    +tipoDato
    +activo
}

class TipoDato {
    <<enumeration>>
    STRING
    INTEGER
    DECIMAL
    BOOLEAN
    DATE
    DATETIME
}

Proceso "1" *-- "0..*" Mensaje : define
Mensaje "1" *-- "1..*" CampoMensaje : contiene
CampoMensaje --> "1" TipoDato : tipo

MessageThrow "0..*" --> "1" Mensaje : envia
MessageCatch "0..*" --> "1" Mensaje : espera

class FlujoMensaje {
    +id
    +canalExterno
    +accionFallo
    +activo
}

class CanalExterno {
    <<enumeration>>
    CORREO
    SERVICIO_WEB
    COLA_MENSAJES
}

class AccionFallo {
    <<enumeration>>
    CONTINUAR
    DERIVAR_ACTIVIDAD_ERROR
    FINALIZAR_PROCESO
}

Proceso "1" *-- "0..*" FlujoMensaje : contiene

FlujoMensaje "0..*" --> "1" Pool : poolOrigen
FlujoMensaje "0..*" --> "1" Pool : poolDestino
FlujoMensaje "0..*" --> "1" Mensaje : transporta

FlujoMensaje "0..*" --> "0..1" MessageThrow : emisor
FlujoMensaje "0..*" --> "0..1" MessageCatch : receptor

FlujoMensaje --> "0..1" CanalExterno : canal
FlujoMensaje --> "0..1" AccionFallo : fallo
FlujoMensaje --> "0..1" Actividad : actividadManejoError
```

## Reglas de dominio principales

1. El NIT de `Empresa` es único globalmente.
2. El correo de `Usuario` es único globalmente.
3. El nombre de `Proceso` es único dentro de su empresa.
4. El nombre de `RolProceso` es único dentro de su empresa.
5. El nombre de `Actividad` es único dentro del proceso.
6. Cada `Actividad` pertenece exactamente a una `Lane`.
7. El `Pool` de una actividad debe coincidir con el `Pool` de su `Lane`.
8. Un `Arco` no puede tener el mismo nodo como origen y destino.
9. El `Arco`, su nodo origen y su nodo destino deben pertenecer al mismo `Pool`.
10. No pueden existir dos arcos idénticos para el mismo par origen-destino.
11. Los gateways exclusivos e inclusivos requieren condiciones en sus arcos salientes.
12. Los gateways paralelos no utilizan condiciones en sus arcos salientes.
13. Un gateway divergente debe tener al menos dos arcos salientes.
14. Un `FlujoMensaje` debe conectar pools diferentes.
15. Un `MessageCatch` de tipo `INICIO` no puede tener arcos entrantes.
16. Una `Lane` no puede eliminarse mientras contenga actividades.
17. Un `RolProceso` no puede eliminarse mientras una lane lo utilice.
18. Una empresa invitada a un proceso compartido tiene acceso de solo lectura.
19. Un pool de caja negra no contiene nodos internos.
20. Los elementos con trazabilidad usan eliminación lógica.

## Alcance inicial

Para la primera entrega se priorizan:

- `Empresa`
- `Usuario`
- `Proceso`
- `HistorialCambio`
- `RolAcceso`
- `EstadoProceso`
- `AccionHistorial`

El resto del modelo queda documentado desde ahora para orientar las siguientes iteraciones de backend y frontend.
