# Guía de ejecución

## Beta-back

### Requisitos

- JDK 21.
- Git.
- Maven 3.9+ o Maven Wrapper.
- Docker opcional.

### Ejecutar

```bash
git clone https://github.com/Facimus-Curiositatem/Beta-back.git
cd Beta-back
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

El backend inicia en `http://localhost:8080`.

Usuario de desarrollo:

- correo: `admin@demo.com`
- contraseña: `admin123`

Swagger:

`http://localhost:8080/swagger-ui/index.html`

H2:

`http://localhost:8080/h2-console`

### Tests

```bash
./mvnw clean verify
```

### Docker

```bash
docker build -t facimus-procesos .
docker run -p 8080:8080 -e JWT_SECRET=mi_clave_secreta facimus-procesos
```

## Beta-Wiki

```bash
git clone https://github.com/Facimus-Curiositatem/Beta-Wiki.git
cd Beta-Wiki/beta
./mvnw spring-boot:run
```

Si Wiki y backend se ejecutan al mismo tiempo, uno debe usar un puerto distinto.
