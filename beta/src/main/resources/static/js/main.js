// Beta Wiki - JavaScript principal
document.addEventListener('DOMContentLoaded', function () {
    inicializarContadorMensaje();
    inicializarValidacionContacto();
});

// Contador de caracteres del formulario de contacto (Actividad 29).
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
        } else if (longitud > MAX) {
            contador.textContent = `Sobran ${longitud - MAX} caracteres`;
            contador.classList.remove('contador-ok');
        } else {
            contador.textContent = `${longitud}/${MAX} caracteres`;
            contador.classList.add('contador-ok');
        }
    }

    mensaje.addEventListener('input', actualizar);
    actualizar();
}

// Validacion del formulario de contacto (Actividad 30).nombre: obligatorio, minimo 3 caracteres sin espacios en blanco.
// telefono: obligatorio, solo digitos, entre 7 y 15 caracteres. asunto: obligatorio, rechaza el valor por defecto del select.
// mensaje: obligatorio, entre 20 y 400
function inicializarValidacionContacto() {
    const form = document.getElementById('form-contacto');
    if (!form) {
        return;
    }

    const campoNombre = document.getElementById('nombre');
    const campoEmail = document.getElementById('email');
    const campoTelefono = document.getElementById('telefono');
    const campoAsunto = document.getElementById('asunto');
    const campoMensaje = document.getElementById('mensaje');
    const feedback = document.getElementById('form-feedback');

    // Un validador por campo: recibe el valor y devuelve el mensaje de error,
    // o cadena vacia si el campo es valido.
    const validadores = {
        nombre: function (valor) {
            const limpio = valor.trim();
            if (limpio.length === 0) {
                return 'El nombre es obligatorio.';
            }
            if (limpio.length < 3) {
                return 'El nombre debe tener minimo 3 caracteres.';
            }
            return '';
        },
        email: function (valor) {
            const limpio = valor.trim();
            if (limpio.length === 0) {
                return 'El correo electronico es obligatorio.';
            }
            // Exige texto antes de @, texto entre @ y el punto, y texto despues del punto.
            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(limpio)) {
                return 'Ingresa un correo valido, con formato usuario@dominio.com';
            }
            return '';
        },
        telefono: function (valor) {
            const limpio = valor.trim();
            if (limpio.length === 0) {
                return 'El telefono es obligatorio.';
            }
            if (!/^\d+$/.test(limpio)) {
                return 'El telefono solo debe contener digitos, sin espacios ni guiones.';
            }
            if (limpio.length < 7 || limpio.length > 15) {
                return 'El telefono debe tener entre 7 y 15 digitos.';
            }
            return '';
        },
        asunto: function (valor) {
            if (!valor || valor.trim().length === 0) {
                return 'Debes seleccionar un asunto.';
            }
            return '';
        },
        mensaje: function (valor) {
            const limpio = valor.trim();
            if (limpio.length === 0) {
                return 'El mensaje es obligatorio.';
            }
            if (limpio.length < 20) {
                return `El mensaje debe tener minimo 20 caracteres (faltan ${20 - limpio.length}).`;
            }
            if (limpio.length > 400) {
                return `El mensaje no puede superar 400 caracteres (sobran ${limpio.length - 400}).`;
            }
            return '';
        }
    };

    const campos = [
        { input: campoNombre, nombre: 'nombre' },
        { input: campoEmail, nombre: 'email' },
        { input: campoTelefono, nombre: 'telefono' },
        { input: campoAsunto, nombre: 'asunto' },
        { input: campoMensaje, nombre: 'mensaje' }
    ];

    // Mostrar los campos de errores
    const tocados = new Set();

    function elementoError(nombreCampo) {
        return form.querySelector(`[data-error-for="${nombreCampo}"]`);
    }

    function validarCampo(campo, mostrarError) {
        if (!campo.input) {
            return true;
        }
        const mensajeError = validadores[campo.nombre](campo.input.value);
        const esValido = mensajeError === '';

        if (mostrarError && tocados.has(campo.nombre)) {
            campo.input.classList.toggle('campo-invalido', !esValido);
            campo.input.classList.toggle('campo-valido', esValido);
            const small = elementoError(campo.nombre);
            if (small) {
                small.textContent = esValido ? '' : mensajeError;
            }
        }
        return esValido;
    }

    function validarTodo(mostrarErrores) {
        let formularioValido = true;
        campos.forEach(function (campo) {
            const valido = validarCampo(campo, mostrarErrores);
            formularioValido = formularioValido && valido;
        });
        return formularioValido;
    }

    function actualizarFeedback(formularioValido) {
        if (!feedback) {
            return;
        }
        const hayAlgunTocado = tocados.size > 0;
        if (!hayAlgunTocado) {
            feedback.hidden = true;
            return;
        }
        feedback.hidden = false;
        if (formularioValido) {
            feedback.textContent = 'Todo listo. Puedes enviar tu mensaje.';
            feedback.classList.remove('error');
            feedback.classList.add('feedback', 'exito');
        } else {
            feedback.textContent = 'Revisa los campos marcados antes de enviar.';
            feedback.classList.remove('exito');
            feedback.classList.add('feedback', 'error');
        }
    }

    // Validacion en vivo: cada vez que el usuario escribe o cambia un campo.
    campos.forEach(function (campo) {
        if (!campo.input) {
            return;
        }
        const marcarYValidar = function () {
            tocados.add(campo.nombre);
            validarCampo(campo, true);
            actualizarFeedback(validarTodo(false));
        };
        campo.input.addEventListener('input', marcarYValidar);
        campo.input.addEventListener('change', marcarYValidar);
        campo.input.addEventListener('blur', marcarYValidar);
    });

    // Validacion final al enviar el formulario.
    form.addEventListener('submit', function (evento) {
        campos.forEach(function (campo) {
            tocados.add(campo.nombre);
        });
        const formularioValido = validarTodo(true);
        actualizarFeedback(formularioValido);

        if (!formularioValido) {
            evento.preventDefault();
            const primerCampoInvalido = campos.find(function (campo) {
                return campo.input && validadores[campo.nombre](campo.input.value) !== '';
            });
            if (primerCampoInvalido && primerCampoInvalido.input) {
                primerCampoInvalido.input.focus();
            }
        }
        // Si es valido, el formulario continua su envio normal (POST /contactenos).
    });
}
