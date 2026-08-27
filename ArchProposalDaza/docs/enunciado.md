El proyecto semestral - Curso Desarrollo Web

24/08/26, 2:53 PM

Inicio

Proyecto

El proyecto semestral

El proyecto del curso consiste en desarrollar un visor y editor de procesos

empresariales: un Sistema de Gestión de Procesos multiempresa que permite a

cada organización registrarse, crear su propio espacio independiente y administrar

usuarios con distintos roles (administrador, editor y solo lectura). El sistema debe

garantizar autenticación segura, control de acceso por empresa y separación de la

información, asegurando que los procesos pertenezcan a la empresa y no a usuarios

individuales.

Es un desarrollo transversal: se construye de forma incremental durante todo el

semestre y cada entrega se apoya en la anterior. Los contenidos vistos en clase se

aplican directamente sobre él, de modo que al final del curso el proyecto integra

persistencia, vistas server-side, servicios REST, frontend SPA, pruebas

automatizadas, seguridad y despliegue.

Qué debe hacer el sistema

Gestión de empresas y usuarios. Registro de la empresa como entidad

independiente, creación del usuario administrador inicial, alta de colaboradores y

asignación de roles de acceso. El inicio de sesión restringe cada usuario a la

información de su propia empresa.

Gestión de procesos. Crear, editar, consultar y eliminar procesos organizacionales,

registrando nombre, descripción, categoría y estado (borrador o publicado). El

sistema debe mantener historial de cambios y trazabilidad, permitir búsquedas y

filtros, y manejar eliminaciones lógicas (estado inactivo) para no perder información

histórica.

Modelado del proceso. Un proceso no es solo una ficha con datos: es un diagrama

completo. El sistema debe permitir modelar todos sus elementos.

https://desarrolloweb.click/proyecto/#entregas

Page 1 of 6

El proyecto semestral - Curso Desarrollo Web

24/08/26, 2:53 PM

Elemento

Qué representa

Actividad

Una tarea del proceso

Arco

La secuencia del flujo entre actividades y gateways dentro de un

mismo pool

Gateway

Un punto de decisión o ramificación: exclusiva, paralela o

inclusiva

Rol de

proceso

La función responsable de una actividad (Analista, Supervisor,

Auditor), no la persona

Pool

Un participante del proceso: la empresa propietaria, un cliente,

un proveedor o un sistema externo

Lane

Una división interna del pool que agrupa las actividades de un

(swimlane)

mismo rol responsable

Mensaje

La comunicación entre pools: un participante envía y otro recibe

(throw / catch)

Correlación

El criterio que indica a qué caso concreto del proceso

corresponde un mensaje

Todos estos elementos son obligatorios. El diagrama debe mantenerse coherente en

todo momento: las actividades pertenecen a una lane, los arcos no cruzan pools, los

mensajes sí lo hacen, y los roles no se pueden eliminar mientras estén en uso.

Ejemplo: cómo se vería un proceso

Este es un proceso de solicitud de vacaciones modelado con todos los elementos del

sistema. Sirve como referencia de lo que un usuario debe poder construir con el

editor.

https://desarrolloweb.click/proyecto/#entregas

Page 2 of 6

El proyecto semestral - Curso Desarrollo Web

24/08/26, 2:53 PM

Radicar
solicitud

a
s
e
r
p
m
E

o
d
a
e

l

p
m
E

o
t
a

i

d
e
m
n

i

e
f
e
J

o
n
a
m
u
H
o
t
n
e

l

a
T

Revisar
solicitud

No

Registrar
rechazo

Sí

Actualizar
días

Mensaje: NotificarAprobación
Correlación: n.º de solicitud

Servicio de correo

Proceso de solicitud de vacaciones con todos los elementos del sistema

Leído sobre el diagrama, cada elemento del sistema aparece así:

En el diagrama

Elemento

El recuadro exterior Empresa

Un pool: el participante dueño del

proceso (HU-21)

Las tres bandas Empleado, Jefe

Lanes, cada una asociada a un rol de

inmediato, Talento Humano

proceso (HU-22, HU-17)

Radicar solicitud, Revisar solicitud, …

Actividades (HU-08)

Las flechas continuas entre

Arcos (HU-11)

actividades

El rombo con la X y sus salidas Sí / No

Un gateway exclusivo: el flujo sigue por

un solo camino (HU-14)

La flecha punteada hacia Servicio de

Un mensaje hacia un participante

correo

externo (HU-25, HU-26)

https://desarrolloweb.click/proyecto/#entregas

Page 3 of 6

El proyecto semestral - Curso Desarrollo Web

24/08/26, 2:53 PM

n.º de solicitud sobre esa flecha

La clave de correlación del mensaje

(HU-28)

El sistema modela este diagrama, no lo ejecuta

El usuario dibuja el proceso, lo guarda y lo consulta. En ningún momento se radica una

solicitud real, ni se envía un correo, ni se descuentan días de vacaciones.

Alcance

El sistema está orientado únicamente a la visualización y edición de procesos, no a

su ejecución. Los usuarios pueden consultar, crear, modificar y organizar procesos,

pero el sistema no dispara flujos automáticos ni ejecuta instancias de proceso.

Esta distinción es importante y no reduce el alcance del modelado: todos los

elementos del proceso se modelan y se validan, ninguno se ejecuta.

El sistema sí debe…

El sistema no debe…

Modelar actividades, arcos, gateways, pools,

Ejecutar instancias del proceso

lanes y mensajes

Asignar roles funcionales a las actividades a

Motor de reglas o de tareas

través de las lanes

automáticas

Definir el contenido y la correlación de cada

Entregar mensajes reales o

mensaje

mantener casos en espera

Documentar el envío hacia sistemas externos

Conectarse a servidores de

y su punto en el flujo

correo, colas o APIs reales

Validar la coherencia del diagrama y advertir

Gestionar credenciales o secretos

sobre errores de modelado

de integración

https://desarrolloweb.click/proyecto/#entregas

Page 4 of 6

El proyecto semestral - Curso Desarrollo Web

24/08/26, 2:53 PM

Registrar historial de cambios y mantener el

—

aislamiento por empresa

El enfoque es académico: se evalúa la correcta aplicación de conceptos de

arquitectura web, separación de responsabilidades, seguridad básica y buenas

prácticas de desarrollo.

Historias de usuario

Se tienen las siguientes de HU: Historias usuario

Allí está el listado completo con su resumen y sus criterios de aceptación, agrupado

en seis bloques: gestión de empresas y usuarios, gestión de procesos, modelado del

proceso, roles de proceso, pools y lanes, y mensajes y colaboración entre pools.

Todas las historias son de obligatorio cumplimiento.

Entregas

El proyecto se evalúa en tres momentos, alineados con los contenidos vistos hasta la

fecha de cada entrega.

Momento

Fecha

Peso

Contenido

Presentación del

12/08/2026

—

Conformación de

enunciado e inicio del

proyecto

Primera entrega ·

Aplicación web con

Spring Boot, Thymeleaf y

JPA

grupos y arranque

14/09/2026

15 %

Modelo de dominio

con JPA, lógica en

capas y vistas con

Thymeleaf para

empresas, usuarios y

procesos

https://desarrolloweb.click/proyecto/#entregas

Page 5 of 6

El proyecto semestral - Curso Desarrollo Web

24/08/26, 2:53 PM

Segunda entrega · API

21/10/2026

25 %

Servicios REST con

REST y frontend con

Angular

Spring Boot, SPA en

Angular que los

consume y

empaquetamiento en

Docker

Entrega final ·

25/11/2026

20 %

Spring Security,

Seguridad, pruebas y

despliegue

pruebas de

integración y E2E,

despliegue y

presentación final

Los criterios de evaluación detallados de cada entrega están en reglas generales y

calificación. Las fechas corresponden al calendario del curso.

https://desarrolloweb.click/proyecto/#entregas

Page 6 of 6


