// Beta Wiki - muestra un documento Markdown de ArchProposalDaza/docs.
// Convierte el Markdown a HTML (marked), dibuja los diagramas (mermaid),
// arma el indice lateral y convierte los enlaces entre .md en enlaces de la wiki.
(function () {
    const datos = window.WIKI_DOC;
    const contenedor = document.getElementById('doc-contenido');
    if (!datos || !contenedor) {
        return;
    }

    // Si el CDN no cargo, se muestra el Markdown tal cual para no dejar la pagina vacia.
    if (typeof window.marked === 'undefined') {
        const pre = document.createElement('pre');
        pre.className = 'doc-fuente';
        pre.textContent = datos.markdown;
        contenedor.replaceChildren(pre);
        return;
    }

    contenedor.innerHTML = window.marked.parse(datos.markdown, { gfm: true });

    envolverTablas();
    reescribirEnlaces();
    const titulos = asignarAnclas();
    construirIndice(titulos);
    dibujarDiagramas().then(irAlAnclaInicial);

    // Las tablas anchas se desplazan dentro de su caja y no rompen el ancho de la pagina.
    function envolverTablas() {
        contenedor.querySelectorAll('table').forEach(function (tabla) {
            const caja = document.createElement('div');
            caja.className = 'tabla-scroll';
            tabla.replaceWith(caja);
            caja.appendChild(tabla);
        });
    }

    // "adr/ADR-004-migracion-rest-jwt.md#estado" -> "/secciones/adr-004-migracion-rest-jwt#estado"
    function reescribirEnlaces() {
        contenedor.querySelectorAll('a[href]').forEach(function (enlace) {
            const href = enlace.getAttribute('href');
            if (/^[a-z]+:/i.test(href)) {
                enlace.target = '_blank';
                enlace.rel = 'noopener';
                return;
            }
            const coincidencia = href.match(/([^\/#?]+)\.md(#.*)?$/i);
            if (coincidencia) {
                enlace.setAttribute('href', datos.baseSecciones + coincidencia[1].toLowerCase() + (coincidencia[2] || ''));
            }
        });
    }

    // Identificadores estilo GitHub para que funcionen los enlaces con "#seccion".
    function asignarAnclas() {
        const usados = {};
        const titulos = [];
        contenedor.querySelectorAll('h1, h2, h3, h4').forEach(function (titulo) {
            let id = titulo.textContent.trim().toLowerCase()
                .replace(/[^\p{L}\p{N}\s_-]/gu, '')
                .replace(/\s/g, '-');
            if (usados[id] !== undefined) {
                usados[id] += 1;
                id = id + '-' + usados[id];
            } else {
                usados[id] = 0;
            }
            titulo.id = id;
            titulo.dataset.texto = titulo.textContent.trim();

            const ancla = document.createElement('a');
            ancla.className = 'ancla';
            ancla.href = '#' + id;
            ancla.textContent = '#';
            ancla.setAttribute('aria-label', 'Enlace a esta seccion');
            titulo.appendChild(ancla);

            if (titulo.tagName === 'H2' || titulo.tagName === 'H3') {
                titulos.push(titulo);
            }
        });
        return titulos;
    }

    // Indice lateral con resaltado de la seccion que se esta leyendo.
    function construirIndice(titulos) {
        const indice = document.getElementById('doc-toc');
        const lista = document.getElementById('doc-toc-lista');
        if (!indice || !lista || titulos.length < 3) {
            return;
        }
        // En pantallas pequenas el indice va arriba del texto: solo se listan las secciones principales.
        if (window.matchMedia('(max-width: 999px)').matches) {
            titulos = titulos.filter(function (titulo) { return titulo.tagName === 'H2'; });
        }
        const enlaces = new Map();
        titulos.forEach(function (titulo) {
            const item = document.createElement('li');
            item.className = titulo.tagName === 'H3' ? 'nivel-3' : 'nivel-2';
            const enlace = document.createElement('a');
            enlace.href = '#' + titulo.id;
            enlace.textContent = titulo.dataset.texto;
            item.appendChild(enlace);
            lista.appendChild(item);
            enlaces.set(titulo, enlace);
        });
        indice.hidden = false;

        if (!('IntersectionObserver' in window)) {
            return;
        }
        const observador = new IntersectionObserver(function (entradas) {
            entradas.forEach(function (entrada) {
                if (entrada.isIntersecting) {
                    lista.querySelectorAll('a.activo').forEach(function (a) { a.classList.remove('activo'); });
                    enlaces.get(entrada.target).classList.add('activo');
                }
            });
        }, { rootMargin: '0px 0px -70% 0px' });
        titulos.forEach(function (titulo) { observador.observe(titulo); });
    }

    // Bloques ```mermaid -> diagramas, con boton para verlos en grande.
    function dibujarDiagramas() {
        const bloques = contenedor.querySelectorAll('pre > code.language-mermaid');
        if (bloques.length === 0 || typeof window.mermaid === 'undefined') {
            return Promise.resolve();
        }
        bloques.forEach(function (codigo) {
            const figura = document.createElement('figure');
            figura.className = 'diagrama';

            const diagrama = document.createElement('div');
            diagrama.className = 'mermaid';
            diagrama.textContent = codigo.textContent;

            const boton = document.createElement('button');
            boton.type = 'button';
            boton.className = 'diagrama-ampliar';
            boton.textContent = 'Ver en grande';
            boton.addEventListener('click', function () {
                const ampliado = figura.classList.toggle('ampliado');
                boton.textContent = ampliado ? 'Cerrar' : 'Ver en grande';
                document.body.classList.toggle('sin-scroll', ampliado);
            });

            figura.appendChild(boton);
            figura.appendChild(diagrama);
            codigo.parentElement.replaceWith(figura);
        });

        document.addEventListener('keydown', function (evento) {
            const abierta = document.querySelector('.diagrama.ampliado');
            if (evento.key === 'Escape' && abierta) {
                abierta.querySelector('.diagrama-ampliar').click();
            }
        });

        window.mermaid.initialize({ startOnLoad: false, theme: 'neutral', securityLevel: 'strict' });
        return window.mermaid.run({ querySelector: '#doc-contenido .mermaid' }).catch(function (error) {
            console.error('No se pudo dibujar un diagrama', error);
        });
    }

    // El navegador no encuentra "#seccion" al cargar porque el contenido se crea despues.
    function irAlAnclaInicial() {
        if (!location.hash) {
            return;
        }
        const destino = document.getElementById(decodeURIComponent(location.hash.slice(1)));
        if (destino) {
            destino.scrollIntoView();
        }
    }
})();