# Beta Wiki

Wiki desarrollada con Spring Boot y Thymeleaf como parte del taller del curso.

## Descripción del proyecto

Beta Wiki es una aplicación web tipo wiki que presenta contenido dinámico organizado por secciones y categorías. Está construida con una arquitectura Modelo-Vista-Controlador (MVC), utilizando Spring Boot para la lógica del servidor y Thymeleaf como motor de plantillas.

Actualmente, la aplicación permite consultar artículos relacionados con tecnologías y conceptos de desarrollo, como Spring Boot, Thymeleaf, arquitectura MVC y control de versiones.

El proyecto también sirve como punto de partida para el desarrollo de un visor y editor de procesos empresariales, en el que cada organización podrá administrar sus usuarios, roles, procesos y diagramas.

> **Nota:** esta aplicación sigue siendo la Wiki académica construida con Thymeleaf. El backend principal del proyecto (`Beta-back`) ya implementa la API REST de gestión empresarial y modelado de procesos; la Wiki documenta ese comportamiento sin reemplazarlo.

## Documentación del backend REST

La referencia de las 28 historias de usuario y su implementación en el servicio REST se encuentra en:

- [Historias de usuario y API REST](HISTORIAS_USUARIO_REST.md)

La documentación cubre autenticación JWT, multitenencia, procesos, historial, actividades, arcos, gateways, roles, pools, lanes, procesos compartidos, permisos, eventos de mensaje, destinos externos y correlación.

## Funcionalidades actuales

- Página principal con la presentación del proyecto.
- Visualización de artículos destacados.
- Organización del contenido por categorías.
- Listado de todas las secciones de la wiki.
- Consulta del detalle de cada artículo.
- Navegación mediante encabezado y pie de página reutilizables.
- Formulario de contacto.
- Validación interactiva de formularios mediante JavaScript.
- Contador de caracteres para el mensaje.
- Mensajes de error y confirmación.
- Diseño adaptable a diferentes tamaños de pantalla.
- Redirección cuando se intenta consultar una sección inexistente.

## Tecnologías utilizadas

- Java 21.
- Spring Boot 4.1.0.
- Spring MVC.
- Thymeleaf.
- Lombok.
- Maven.
- HTML5.
- CSS3.
- JavaScript.
- JUnit 5.

## Arquitectura del proyecto

El proyecto utiliza el patrón **Modelo-Vista-Controlador (MVC)**:

- **Modelo:** representa la información de cada página de la wiki.
- **Repositorio:** administra las páginas disponibles en memoria.
- **Controlador:** recibe las solicitudes, consulta el repositorio y envía la información a las vistas.
- **Vista:** genera las páginas HTML dinámicamente mediante Thymeleaf.
- **Recursos estáticos:** contienen los estilos CSS y la validación del formulario mediante JavaScript.

Actualmente, la información de la wiki se almacena en memoria. Esto significa que todavía no existe una conexión con una base de datos y que los datos se reinician cada vez que la aplicación vuelve a ejecutarse.

## Estructura del proyecto

```text
Beta-Wiki/
├── README.md
└── beta/
    ├── .mvn/
    ├── mvnw
    ├── mvnw.cmd
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/wiki/beta/
        │   │   ├── BetaApplication.java
        │   │   ├── controller/
        │   │   │   └── WikiController.java
        │   │   ├── model/
        │   │   │   └── WikiPage.java
        │   │   └── repository/
        │   │       └── WikiRepository.java
        │   └── resources/
        │       ├── application.properties
        │       ├── static/
        │       │   ├── css/
        │       │   │   └── styles.css
        │       │   └── js/
        │       │       └── main.js
        │       └── templates/
        │           ├── fragments/
        │           │   ├── footer.html
        │           │   └── header.html
        │           ├── contactenos.html
        │           ├── detalle.html
        │           ├── inicio.html
        │           ├── layout.html
        │           └── secciones.html
        └── test/
            └── java/com/wiki/beta/
                └── BetaApplicationTests.java
```

## Cómo ejecutar

### Prerrequisitos

Para ejecutar el proyecto se necesita:

- Java Development Kit (JDK) 21 o superior.
- Git.
- Maven 3.9 o superior, en caso de no utilizar Maven Wrapper.
- Conexión a Internet durante la primera descarga de dependencias.

El repositorio incluye **Maven Wrapper**, por lo que no es obligatorio instalar Maven de manera global.

Puedes comprobar las versiones instaladas con:

```bash
java --version
mvn --version
git --version
```

### Pasos

#### 1. Clonar el repositorio

```bash
git clone https://github.com/Facimus-Curiositatem/Beta-Wiki.git
```

#### 2. Entrar en la carpeta de la aplicación

```bash
cd Beta-Wiki/beta
```

#### 3. Ejecutar la aplicación

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

#### 4. Abrir la aplicación

Una vez iniciado el servidor, abre la siguiente dirección en el navegador:

```text
http://localhost:8080
```

Para detener la aplicación, presiona `Ctrl + C` en la terminal.

## Rutas disponibles

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/` | Muestra la página principal |
| `GET` | `/secciones` | Muestra los artículos agrupados por categoría |
| `GET` | `/secciones/{slug}` | Muestra el detalle de un artículo |
| `GET` | `/contactenos` | Muestra el formulario de contacto |
| `POST` | `/contactenos` | Procesa el formulario y muestra una confirmación |

### Ejemplos

```text
http://localhost:8080/secciones/spring-boot
http://localhost:8080/secciones/thymeleaf
http://localhost:8080/secciones/arquitectura-mvc
http://localhost:8080/secciones/control-de-versiones
```

## Contenido de la wiki

La versión actual contiene artículos sobre:

- Spring Boot.
- Thymeleaf.
- Arquitectura MVC.
- Control de versiones con Git.

Cada artículo tiene:

- Un título.
- Un identificador único o `slug`.
- Una categoría.
- Una descripción.

## Formulario de contacto

La aplicación incluye un formulario con los siguientes campos:

- Nombre completo.
- Correo electrónico.
- Teléfono.
- Asunto.
- Mensaje.

La validación se realiza en el navegador mediante JavaScript e incluye:

- Nombre de mínimo tres caracteres.
- Correo electrónico con formato válido.
- Teléfono compuesto por entre 7 y 15 dígitos.
- Selección obligatoria de un asunto.
- Mensaje de entre 20 y 400 caracteres.
- Contador de caracteres en tiempo real.
- Mensajes individuales de validación.
- Indicador general del estado del formulario.

En la versión actual, el formulario procesa la solicitud y muestra un mensaje de confirmación. Sin embargo, todavía no almacena la información ni envía correos electrónicos.

## Pruebas

Para ejecutar las pruebas en Linux o macOS:

```bash
./mvnw test
```

En Windows:

```powershell
.\mvnw.cmd test
```

Actualmente, el proyecto incluye una prueba básica que comprueba que el contexto de Spring Boot se cargue correctamente.

## Estado funcional del backend

El backend REST ya implementa las 28 historias de usuario del alcance actual. Entre las capacidades disponibles se encuentran:

- registro de empresas y administración de usuarios;
- autenticación JWT y separación segura por empresa;
- CRUD y consulta de procesos con historial;
- actividades, arcos, gateways, pools y lanes;
- roles de proceso y permisos de estructura;
- procesos compartidos entre empresas en modo solo lectura;
- eventos Message Throw y Message Catch;
- correlación de mensajes y destinos externos;
- validación de coherencia antes de publicación;
- eliminación lógica y análisis de impacto;
- pruebas unitarias, de integración y reglas de arquitectura.

La evolución futura se concentra en la experiencia de usuario, integraciones reales con sistemas externos, automatización operativa y despliegue productivo.

## Integrantes

| Nombre | Rol |
|---|---|
| Samuel Bonilla | Desarrollador |
| Samuel Giraldo | Desarrollador |
| Juan Guillermo Gómez | Desarrollador |
| Andrés Ospina | Desarrollador |

## Estado del proyecto

El proyecto se encuentra en fase **beta** y continúa en desarrollo con fines académicos.
