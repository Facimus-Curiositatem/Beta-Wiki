# Convencion de Ramas

## Rama principal

- `main` — Rama estable con el codigo listo para entrega/despliegue.

## Ramas de funcionalidad

Cada integrante trabaja en su propia rama, nombrada segun la funcionalidad asignada:

```
feature/<nombre-funcionalidad>
```

### Ejemplos

| Integrante | Rama                        | Funcionalidad              |
|------------|-----------------------------|----------------------------|
| Miembro 1  | `feature/layout-fragmentos` | Layout base y fragmentos   |
| Miembro 2  | `feature/modelo-datos`      | Modelo y datos en memoria  |
| Miembro 3  | `feature/controlador-wiki`  | Controlador y rutas        |
| Miembro 4  | `feature/contactenos`       | Formulario de contacto     |

## Flujo de trabajo

1. Crear la rama desde `main`: `git checkout -b feature/<nombre>`
2. Hacer commits frecuentes con mensajes descriptivos.
3. Al terminar, abrir un Pull Request hacia `main`.
4. Revision por al menos un companero antes de hacer merge.
5. Resolver conflictos localmente antes del merge.
