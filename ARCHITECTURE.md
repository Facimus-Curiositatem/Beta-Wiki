# Separacion de Responsabilidades (MVC)

## Modelo (`model/`)

- `WikiPage`: Clase POJO con atributos de datos (titulo, slug, contenido, categoria).
- No contiene logica de negocio ni dependencias de framework.

## Repositorio (`repository/`)

- `WikiRepository`: Acceso a datos en memoria.
- Encapsula las consultas (buscar por slug, por categoria, listar todo).
- No conoce las vistas ni los controladores.

## Controlador (`controller/`)

- `WikiController`: Recibe las peticiones HTTP y prepara los datos para las vistas.
- Solo usa `model.addAttribute()` para pasar datos al template.
- No contiene logica de negocio ni manipulacion de datos.

## Vistas (`templates/`)

- Plantillas Thymeleaf que solo renderizan los datos recibidos del modelo.
- No realizan consultas ni contienen logica de negocio.
- Usan fragmentos reutilizables para header, footer y layout.
