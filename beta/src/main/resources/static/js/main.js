// Beta Wiki - JavaScript principal
document.addEventListener('DOMContentLoaded', function () {
    inicializarContadorMensaje();
});

// Contador de caracteres del formulario de contacto (Actividad 29).
// Guardado por existencia de elementos para no fallar en otras vistas.
function inicializarContadorMensaje() {
    const MIN = 20;
    const MAX = 400;
    const mensaje = document.getElementById('mensaje');
    const contador = document.getElementById('contador-mensaje');

    if (!mensaje || !contador) {
        return;
    }

    function actualizar() {
        const longitud = mensaje.value.trim().length;
        if (longitud < MIN) {
            contador.textContent = `Faltan ${MIN - longitud} caracteres`;
            contador.classList.remove('contador-ok');
        } else {
            contador.textContent = `${longitud}/${MAX} caracteres`;
            contador.classList.add('contador-ok');
        }
    }

    mensaje.addEventListener('input', actualizar);
    actualizar();
}
