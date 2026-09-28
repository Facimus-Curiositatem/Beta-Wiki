package com.wiki.beta.repository;

import java.util.List;

import com.wiki.beta.model.WikiPage;

/**
 * Guias complementarias para entender, ejecutar y validar el proyecto Beta.
 */
final class BetaProjectGuides {

    private BetaProjectGuides() {
    }

    static List<WikiPage> pages() {
        return List.of(
                arquitecturaVisual(),
                guiaEjecucion(),
                ejemplosApi(),
                pruebasCalidad());
    }

    private static WikiPage arquitecturaVisual() {
        String contenido = """
                <div class="wiki-doc">
                    <p class="wiki-resumen">
                        Esta pagina muestra como se conectan las piezas principales de Beta:
                        cliente, seguridad JWT, Controllers, Services, Repositories, dominio BPMN,
                        base de datos y herramientas de calidad.
                    </p>

                    <h2>Vista general</h2>
                    <div class="wiki-flow">
                        Cliente / Frontend
                        &rarr; API REST /api/v1
                        &rarr; JWT + SecurityConfig
                        &rarr; Controller
                        &rarr; Service
                        &rarr; Repository JPA
                        &rarr; H2 / PostgreSQL
                    </div>

                    <h2>Capas y responsabilidad</h2>
                    <ul>
                        <li><strong>Cliente:</strong> consume JSON y envia Bearer JWT.</li>
                        <li><strong>Security:</strong> valida token, usuario activo, tenant y rol.</li>
                        <li><strong>Controller:</strong> transforma HTTP/JSON en llamadas de aplicacion.</li>
                        <li><strong>Service:</strong> ejecuta reglas de negocio, transacciones y auditoria.</li>
                        <li><strong>Repository:</strong> encapsula consultas Spring Data JPA.</li>
                        <li><strong>Model:</strong> representa Empresa, Usuario, Proceso y elementos BPMN.</li>
                    </ul>

                    <h2>Modulos del backend</h2>
                    <div class="wiki-flow">
                        common + config + security
                        <br>
                        &#8595;
                        <br>
                        gestion: empresas / usuarios / procesos / roles / historial
                        <br>
                        &#8595;
                        <br>
                        modelado: pools / lanes / actividades / gateways / arcos / mensajes
                    </div>

                    <h2>Flujo de una peticion autenticada</h2>
                    <ol>
                        <li>El cliente envia <code>Authorization: Bearer &lt;jwt&gt;</code>.</li>
                        <li><code>JwtAuthenticationFilter</code> valida el token con <code>JwtService</code>.</li>
                        <li>Se reconstruye <code>ApiPrincipal</code> con usuario, empresa y rol.</li>
                        <li><code>SecurityConfig</code> valida acceso al endpoint.</li>
                        <li>El Controller toma <code>principal.empresaId()</code> y delega al Service.</li>
                        <li>El Service aplica reglas y consulta Repositories tenant-aware.</li>
                        <li>El Controller responde con un DTO, nunca con detalles internos de seguridad.</li>
                    </ol>

                    <h2>Diagrama de despliegue simplificado</h2>
                    <div class="wiki-flow">
                        Navegador / Angular (localhost:4200)
                        &rarr; HTTP/JSON
                        &rarr; Beta-back (localhost:8080)
                        &rarr; H2 local o PostgreSQL
                    </div>

                    <h2>Archivos para estudiar esta arquitectura</h2>
                    <ul class="file-list">
                        <li><code>src/main/java/com/facimus/procesos/security/SecurityConfig.java</code></li>
                        <li><code>src/main/java/com/facimus/procesos/security/JwtAuthenticationFilter.java</code></li>
                        <li><code>src/main/java/com/facimus/procesos/security/ApiPrincipal.java</code></li>
                        <li><code>src/main/java/com/facimus/procesos/gestion/controller/ProcesoController.java</code></li>
                        <li><code>src/main/java/com/facimus/procesos/gestion/service/ProcesoService.java</code></li>
                        <li><code>src/main/java/com/facimus/procesos/gestion/repository/ProcesoRepository.java</code></li>
                        <li><code>src/main/java/com/facimus/procesos/modelado/service/ValidacionModeloService.java</code></li>
                    </ul>

                    <div class="wiki-result">
                        <strong>Idea clave:</strong> el Controller conoce HTTP, el Service conoce el negocio
                        y el Repository conoce persistencia. La seguridad y el tenant se resuelven antes de
                        llegar a la regla de negocio.
                    </div>
                </div>
                """;
        return new WikiPage("Arquitectura visual de Beta", "arquitectura-sistema-beta",
                contenido, "Guias del Proyecto");
    }

    private static WikiPage guiaEjecucion() {
        String contenido = """
                <div class="wiki-doc">
                    <p class="wiki-resumen">
                        Guia practica para levantar Beta-back y Beta-Wiki localmente, ejecutar pruebas
                        y comprobar Swagger, H2 y Docker.
                    </p>

                    <h2>Prerrequisitos</h2>
                    <ul>
                        <li>JDK 21.</li>
                        <li>Git.</li>
                        <li>Maven 3.9+ o Maven Wrapper incluido en los repositorios.</li>
                        <li>Docker solo si se quiere probar la imagen del backend.</li>
                    </ul>

                    <h2>Levantar Beta-back</h2>
                    <pre class="code-block"><code>git clone https://github.com/Facimus-Curiositatem/Beta-back.git
cd Beta-back
./mvnw spring-boot:run</code></pre>
                    <p>
                        En Windows se usa <code>.\mvnw.cmd spring-boot:run</code>.
                        El backend inicia en <code>http://localhost:8080</code>.
                    </p>

                    <h2>Usuario de desarrollo</h2>
                    <p>
                        En entorno local el inicializador del backend crea una empresa demo con
                        <code>admin@demo.com</code> y contrasena <code>admin123</code>.
                    </p>

                    <h2>Swagger / OpenAPI</h2>
                    <p>
                        Con el backend activo, la documentacion interactiva esta disponible en
                        <code>http://localhost:8080/swagger-ui/index.html</code>.
                        El boton <strong>Authorize</strong> permite pegar el JWT obtenido en login.
                    </p>

                    <h2>Base de datos local</h2>
                    <p>
                        El perfil de desarrollo usa H2 persistida en archivo:
                        <code>jdbc:h2:file:./data/procesos</code>. La consola H2 queda habilitada en
                        <code>/h2-console</code>.
                    </p>

                    <h2>Variables de entorno principales</h2>
                    <ul class="file-list">
                        <li><code>JWT_SECRET</code> - secreto de firma JWT.</li>
                        <li><code>JWT_EXPIRATION_SECONDS</code> - expiracion, default 1800 segundos.</li>
                        <li><code>CORS_ALLOWED_ORIGINS</code> - default <code>http://localhost:4200</code>.</li>
                        <li><code>DB_HOST</code>, <code>DB_PORT</code>, <code>DB_NAME</code>,
                            <code>DB_USER</code>, <code>DB_PASSWORD</code> - PostgreSQL de produccion.</li>
                    </ul>

                    <h2>Ejecutar tests del backend</h2>
                    <pre class="code-block"><code>./mvnw clean verify</code></pre>
                    <p>
                        <code>verify</code> ejecuta pruebas, JaCoCo y las verificaciones configuradas en Maven.
                    </p>

                    <h2>Ejecutar con Docker</h2>
                    <pre class="code-block"><code>docker build -t facimus-procesos .
docker run -p 8080:8080 -e JWT_SECRET=mi_clave_secreta facimus-procesos</code></pre>

                    <h2>Levantar Beta-Wiki</h2>
                    <pre class="code-block"><code>git clone https://github.com/Facimus-Curiositatem/Beta-Wiki.git
cd Beta-Wiki/beta
./mvnw spring-boot:run</code></pre>
                    <p>
                        La Wiki tambien inicia por defecto en el puerto 8080, por lo que si se desea
                        ejecutar al mismo tiempo que Beta-back debe asignarse otro puerto a uno de los dos.
                    </p>

                    <div class="wiki-result">
                        <strong>Checklist:</strong> backend arriba, login funcionando, Swagger accesible,
                        tests verdes y Wiki navegable.
                    </div>
                </div>
                """;
        return new WikiPage("Guia de ejecucion y puesta en marcha", "guia-ejecucion-beta",
                contenido, "Guias del Proyecto");
    }

    private static WikiPage ejemplosApi() {
        String contenido = """
                <div class="wiki-doc">
                    <p class="wiki-resumen">
                        Ejemplos de consumo de la API para entender el flujo completo:
                        autenticarse, enviar JWT y operar recursos REST.
                    </p>

                    <h2>1. Iniciar sesion</h2>
                    <pre class="code-block"><code>curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@demo.com",
    "password": "admin123"
  }'</code></pre>
                    <p>
                        La respuesta contiene el token que se reutiliza en
                        <code>Authorization: Bearer &lt;token&gt;</code>.
                    </p>

                    <h2>2. Crear un proceso</h2>
                    <pre class="code-block"><code>curl -X POST http://localhost:8080/api/v1/procesos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Proceso de compras",
    "descripcion": "Flujo de compras de la empresa",
    "categoria": "Operativo"
  }'</code></pre>

                    <h2>3. Consultar procesos paginados</h2>
                    <pre class="code-block"><code>curl "http://localhost:8080/api/v1/procesos?pagina=0" \
  -H "Authorization: Bearer $TOKEN"</code></pre>
                    <p>
                        La API responde con <code>PageResponse</code>: contenido, pagina, tamano,
                        total de elementos y total de paginas.
                    </p>

                    <h2>4. Crear una actividad</h2>
                    <pre class="code-block"><code>curl -X POST http://localhost:8080/api/v1/lanes/1/actividades \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Validar solicitud",
    "descripcion": "Revision inicial",
    "tipoActividad": "USUARIO",
    "posicionX": 120,
    "posicionY": 80
  }'</code></pre>

                    <h2>5. Compartir un proceso</h2>
                    <pre class="code-block"><code>curl -X POST http://localhost:8080/api/v1/procesos/10/compartidos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "empresaInvitadaId": 2
  }'</code></pre>

                    <h2>6. Ejemplo de error de negocio</h2>
                    <p>
                        Si el cliente intenta crear un recurso que viola una regla, el backend
                        centraliza la respuesta con <code>ProblemDetail</code>.
                    </p>
                    <pre class="code-block"><code>{
  "status": 409,
  "title": "Conflicto",
  "detail": "Descripcion de la regla de negocio incumplida"
}</code></pre>

                    <h2>Como leer un ejemplo</h2>
                    <ol>
                        <li>Identificar el metodo HTTP.</li>
                        <li>Verificar si el endpoint requiere JWT.</li>
                        <li>Revisar el DTO de request correspondiente.</li>
                        <li>Seguir Controller &rarr; Service &rarr; Repository en la HU documentada.</li>
                        <li>Consultar Swagger para el contrato exacto vigente.</li>
                    </ol>

                    <div class="wiki-result">
                        <strong>Recomendacion:</strong> usar Swagger como fuente interactiva del contrato
                        y esta pagina como guia para entender el flujo tecnico.
                    </div>
                </div>
                """;
        return new WikiPage("Ejemplos practicos de la API REST", "ejemplos-api-rest",
                contenido, "Guias del Proyecto");
    }

    private static WikiPage pruebasCalidad() {
        String contenido = """
                <div class="wiki-doc">
                    <p class="wiki-resumen">
                        Beta no depende solo de que el proyecto compile. El pipeline valida tests,
                        cobertura, arquitectura, analisis estatico y construccion Docker antes de
                        considerar estable un cambio.
                    </p>

                    <h2>Piramide de validacion del proyecto</h2>
                    <div class="wiki-flow">
                        Tests unitarios / Controller tests
                        &rarr; Maven verify
                        &rarr; JaCoCo
                        &rarr; ArchUnit
                        &rarr; SonarCloud
                        &rarr; Docker build
                    </div>

                    <h2>Pruebas automatizadas</h2>
                    <p>
                        Los Services se prueban con JUnit 5 y Mockito para validar reglas de negocio
                        sin depender de HTTP. Los Controllers usan pruebas MVC para comprobar rutas,
                        status codes, seguridad y serializacion.
                    </p>

                    <h2>JaCoCo</h2>
                    <p>
                        <code>jacoco-maven-plugin</code> genera el reporte XML/HTML utilizado por el
                        pipeline y por SonarCloud. Maven tiene ademas una regla minima global configurada.
                    </p>

                    <h2>Coverage on New Code</h2>
                    <p>
                        La regla de trabajo del equipo es mantener el codigo nuevo por encima del
                        <strong>80%</strong> de cobertura. Cuando Sonar marca archivos nuevos con poca
                        cobertura, se agregan pruebas sobre comportamiento real y ramas de negocio,
                        evitando tests artificiales de getters.
                    </p>

                    <h2>ArchUnit</h2>
                    <p>
                        Las reglas de arquitectura se ejecutan en un job independiente de GitHub Actions.
                        Su objetivo es detectar dependencias indebidas entre capas y proteger la
                        separacion Controller / Service / Repository.
                    </p>

                    <h2>SonarCloud</h2>
                    <p>
                        El proyecto configura <code>Facimus-Curiositatem_Beta-back</code> como
                        <code>sonar.projectKey</code>. En los pushes soportados, GitHub Actions ejecuta
                        <code>mvn verify sonar:sonar</code> utilizando el reporte JaCoCo.
                    </p>

                    <h2>GitHub Actions</h2>
                    <ul>
                        <li><strong>Build &amp; Test Ubuntu:</strong> <code>mvn clean verify</code>.</li>
                        <li><strong>Build &amp; Test Windows:</strong> misma validacion en otro SO.</li>
                        <li><strong>Architecture Rules:</strong> ejecuta ArchUnit.</li>
                        <li><strong>Docker Image:</strong> construye la imagen y comprueba que inicia.</li>
                        <li><strong>SonarQube Analysis:</strong> analiza calidad en eventos push.</li>
                    </ul>

                    <h2>Archivos de calidad</h2>
                    <ul class="file-list">
                        <li><code>pom.xml</code> - JaCoCo, Sonar y dependencias de test.</li>
                        <li><code>.github/workflows/ci.yml</code> - pipeline.</li>
                        <li><code>src/test/java/**</code> - suite automatizada.</li>
                        <li><code>Dockerfile</code> - imagen validada por CI.</li>
                    </ul>

                    <h2>Como validar antes de un PR</h2>
                    <pre class="code-block"><code>./mvnw clean verify
git status
git diff main...HEAD</code></pre>

                    <div class="wiki-result">
                        <strong>Criterio:</strong> una funcionalidad no se considera terminada solo por
                        existir en codigo; debe tener pruebas, respetar arquitectura y pasar el pipeline.
                    </div>
                </div>
                """;
        return new WikiPage("Pruebas, cobertura y calidad", "pruebas-calidad-beta",
                contenido, "Guias del Proyecto");
    }
}
