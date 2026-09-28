# Ejemplos de consumo de la API REST

## Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@demo.com","password":"admin123"}'
```

Guarda el JWT retornado y úsalo como Bearer Token.

## Crear proceso

```bash
curl -X POST http://localhost:8080/api/v1/procesos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre":"Proceso de compras",
    "descripcion":"Flujo de compras de la empresa",
    "categoria":"Operativo"
  }'
```

## Consultar procesos

```bash
curl "http://localhost:8080/api/v1/procesos?pagina=0" \
  -H "Authorization: Bearer $TOKEN"
```

## Crear actividad

```bash
curl -X POST http://localhost:8080/api/v1/lanes/1/actividades \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre":"Validar solicitud",
    "descripcion":"Revision inicial",
    "tipoActividad":"USUARIO",
    "posicionX":120,
    "posicionY":80
  }'
```

## Compartir proceso

```bash
curl -X POST http://localhost:8080/api/v1/procesos/10/compartidos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"empresaInvitadaId":2}'
```

## Error de negocio

Las reglas de negocio se exponen mediante `ProblemDetail`, normalmente con HTTP 409 cuando existe conflicto.
