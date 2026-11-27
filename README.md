# InvexEmpleados · Employees API

Microservicio REST para gestionar empleados, construido como solución al ejercicio técnico de **Desarrollador Java Senior** de INVEX.

La idea fue resolverlo como lo haría en un proyecto real: un servicio pequeño y fácil de leer, con validaciones serias, errores claros para quien consume la API, pruebas que de verdad cubren el comportamiento y un camino sencillo para levantarlo en local, en Docker o en un pipeline.

> El código está en inglés (como pide el ejercicio). Esta documentación está en español para facilitar la revisión.

---

## Tabla de contenido

1. [Stack](#stack)
2. [Cómo ejecutarlo](#cómo-ejecutarlo)
3. [Endpoints](#endpoints)
4. [Modelo de datos y validaciones](#modelo-de-datos-y-validaciones)
5. [Manejo de errores](#manejo-de-errores)
6. [Seguridad y DevSecOps](#seguridad-y-devsecops)
7. [Logs y trazabilidad](#logs-y-trazabilidad)
8. [Pruebas](#pruebas)
9. [Documentación del contrato API](#documentación-del-contrato-api)
10. [Arquitectura y decisiones](#arquitectura-y-decisiones)
11. [Git Flow](#git-flow)
12. [Entregables y evidencias](#entregables-y-evidencias)
13. [Cobertura del ejercicio](#cobertura-del-ejercicio)

---

## Stack

| Tema | Elección |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 2.7.18 (Web, Data JPA, Validation, Security, Actuator) |
| Persistencia | Hibernate / Spring Data JPA |
| Base de datos | H2 en memoria |
| Documentación | springdoc-openapi (Swagger UI + contrato OpenAPI 3) |
| Pruebas | JUnit 5, Mockito, AssertJ, MockMvc, `@DataJpaTest`, JaCoCo |
| Build | Maven |
| Contenedores | Dockerfile multi-stage + docker-compose |
| CI/CD | GitHub Actions, con escaneo de vulnerabilidades (Trivy) |

---

## Cómo ejecutarlo

### Requisitos

- **JDK 17** (probado con JDK 17 en Docker y en local).
- **Docker** (opcional, solo para la opción en contenedor).

### Opción 1 · Local (lo más rápido)

```bash
./mvnw spring-boot:run          # Linux / macOS / Git Bash
mvnw.cmd spring-boot:run        # Windows (cmd / PowerShell)
```

Listo en `http://localhost:8080`. La base es en memoria: se crea al arrancar y se borra al detener.

Credenciales por defecto **solo para local**: `invex` / `invex-local-only`. Puedes cambiarlas sin tocar código:

```bash
API_USERNAME=miusuario API_PASSWORD=miclave ./mvnw spring-boot:run
```

### Opción 2 · Docker

```bash
cp .env.example .env     # y define tus credenciales
docker compose up -d --build
```

- El contenedor tiene su propio `HEALTHCHECK` contra `/actuator/health`.
- Para apagarlo: `docker compose down`. Como la base es en memoria, los datos se pierden al detener el contenedor.

`docker-compose` falla a propósito si faltan las credenciales en `.env`: preferí eso a arrancar con contraseñas por defecto.

### Opción 3 · Solo el jar

```bash
./mvnw clean package
java -jar target/employees-api.jar
```

### Variables de entorno

| Variable | Default | Uso |
|---|---|---|
| `API_USERNAME` / `API_PASSWORD` | `invex` / `invex-local-only` | Credenciales HTTP Basic de la API |
| `SERVER_PORT` | `8080` | Puerto HTTP |
| `LOG_FILE` | `logs/employees-api.log` | Archivo de logs |

### URLs útiles

| Qué | URL | ¿Pide credenciales? |
|---|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html | No (los endpoints sí, usa el botón **Authorize**) |
| Contrato OpenAPI (JSON) | http://localhost:8080/v3/api-docs | No |
| Health check | http://localhost:8080/actuator/health | No |
| Info del servicio | http://localhost:8080/actuator/info | No |
| Consola de la base de datos (H2) | http://localhost:8080/h2-console | Sí (ver abajo) |

### Consola de la base de datos (H2)

La base de datos es H2 en memoria y se puede consultar desde el navegador mientras la aplicación está en ejecución:

1. Abre **http://localhost:8080/h2-console**.
2. El navegador pedirá usuario y contraseña: son las **credenciales de la API** (`invex` / `invex-local-only` en local, o las que hayas definido en `API_USERNAME` / `API_PASSWORD`).
3. En la pantalla de login de H2 captura exactamente estos datos:

   | Campo | Valor |
   |---|---|
   | Driver Class | `org.h2.Driver` |
   | JDBC URL | `jdbc:h2:mem:employeesdb` |
   | User Name | `sa` |
   | Password | *(vacío)* |

4. Pulsa **Connect**. Verás la tabla `EMPLOYEES` y podrás ejecutar consultas, por ejemplo `SELECT * FROM EMPLOYEES;`.

> La base vive en memoria: empieza vacía en cada arranque y se borra al detener la aplicación. Para ver datos, crea empleados primero con la API (Swagger, Postman o curl).

---

## Endpoints

Base path: `/employees`. Todos requieren HTTP Basic.

| Método | Ruta | Qué hace | Respuestas |
|---|---|---|---|
| `GET` | `/employees` | Lista todos los empleados (ordenados por id) | `200` |
| `GET` | `/employees/{id}` | Detalle de un empleado | `200`, `400` id no numérico, `404` |
| `POST` | `/employees` | Crea **uno o varios** empleados en la misma petición | `201`, `400`, `415` |
| `PUT` | `/employees/{id}` | Actualiza **todos o algunos** campos | `200`, `400`, `404` |
| `DELETE` | `/employees/{id}` | Elimina un empleado | `204`, `404` |
| `GET` | `/employees/search?name={name}` | Búsqueda parcial por nombre | `200`, `400` si falta o está vacío |

Cualquier endpoint puede responder `401` sin credenciales y `500` ante un error inesperado (sin exponer detalles internos).

### Detalles que vale la pena mencionar

- **POST acepta un objeto o un arreglo.** Mandar `{...}` o `[{...}, {...}]` funciona igual; la respuesta siempre es una lista. El lote es **atómico**: si un empleado es inválido, no se guarda ninguno y el error te dice exactamente cuál (`requests[1].age`). Máximo 100 por petición.
- **PUT es parcial.** Solo se aplican los campos que envías; los que omites (o mandas en `null`) conservan su valor. `id` y `createdAt` nunca se pueden modificar.
- **La búsqueda** no distingue mayúsculas/minúsculas y busca en primer nombre, segundo nombre y ambos apellidos: `ram` encuentra a *Ana García **Ram**írez* y a *Luis **Ram**írez*. Los caracteres `%` y `_` se tratan como texto, no como comodines.
- **Campos desconocidos se rechazan** con `400`. Si alguien manda `"fristName"` por error, prefiero que se entere a que el dato se pierda en silencio.

### Ejemplos rápidos con curl

```bash
# Crear varios
curl -u invex:invex-local-only -H "Content-Type: application/json" -X POST http://localhost:8080/employees -d '[
  {"firstName":"Juan","middleName":"Carlos","paternalLastName":"Pérez","maternalLastName":"López",
   "age":30,"sex":"MALE","birthDate":"15-03-1994","position":"Backend Developer"},
  {"firstName":"Ana","paternalLastName":"García","maternalLastName":"Ramírez",
   "age":28,"sex":"FEMALE","birthDate":"02-07-1996","position":"QA Engineer"}
]'

# Actualizar solo el puesto
curl -u invex:invex-local-only -H "Content-Type: application/json" -X PUT http://localhost:8080/employees/1 \
  -d '{"position":"Senior Backend Developer"}'

# Buscar
curl -u invex:invex-local-only "http://localhost:8080/employees/search?name=ram"
```

---

## Modelo de datos y validaciones

| Campo JSON | Tipo | Obligatorio al crear | Reglas |
|---|---|---|---|
| `id` | número | — (lo genera la BD) | Solo lectura |
| `firstName` | texto | Sí | Máx. 50, solo letras (con acentos), espacios, `'`, `.`, `-` |
| `middleName` | texto | No | Mismas reglas de nombre |
| `paternalLastName` | texto | Sí | Mismas reglas de nombre |
| `maternalLastName` | texto | No | Mismas reglas de nombre |
| `age` | entero | Sí | Entre 18 y 100 (campo libre, no se calcula de la fecha) |
| `sex` | enum | Sí | `MALE`, `FEMALE` u `OTHER` |
| `birthDate` | fecha | Sí | Formato **`dd-MM-yyyy`**, debe estar en el pasado |
| `position` | texto | Sí | Máx. 100, no puede ser solo espacios |
| `createdAt` | fecha-hora | — | **Automático** al insertar (`dd-MM-yyyy HH:mm:ss`), solo lectura |
| `active` | booleano | No | Si no se envía, el empleado se crea **activo** |

Los textos se guardan sin espacios sobrantes al inicio o al final.

---

## Manejo de errores

Todos los errores salen con la misma forma, inspirada en **RFC 7807 (Problem Details)** y con `Content-Type: application/problem+json`:

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/employees",
  "requestId": "6f1c2a9e-3c1d-4b8e-9f3a-2d4c5b6a7e8f",
  "timestamp": "2026-11-26T13:25:22.512-06:00",
  "errors": [
    { "field": "requests[0].age", "message": "must be at least 18" },
    { "field": "requests[0].sex", "message": "is required (MALE, FEMALE or OTHER)" }
  ]
}
```

- `requestId` es el mismo valor del header `X-Request-Id` y del log, así que si algo falla basta con compartir ese id para encontrarlo.
- Nunca se devuelve un stack trace; el detalle técnico se queda en el log.

| Situación | Status |
|---|---|
| Campos inválidos, JSON mal formado, fecha con otro formato, enum desconocido, campo desconocido, id no numérico, falta `name` | `400` |
| Sin credenciales o incorrectas | `401` |
| Empleado inexistente | `404` |
| Método no soportado | `405` |
| Body que no es JSON | `415` |
| Conflicto de integridad en BD | `409` |
| Cualquier otra cosa | `500` (mensaje genérico) |

Las excepciones de negocio extienden de `ApiException`, que ya sabe su status HTTP. Agregar un error nuevo es crear una clase; el `GlobalExceptionHandler` lo atiende sin cambios.

---

## Seguridad y DevSecOps

- **HTTP Basic, sin sesión (stateless).** Las credenciales vienen de variables de entorno, nunca del código. CSRF está deshabilitado porque no hay cookies ni sesiones de navegador.
- **Superficie pública mínima:** solo `health`, `info` y la documentación. El resto de actuator no se expone y `health` no muestra detalles internos. La consola de H2 también pide las credenciales de la API.
- **El 401 también responde JSON** con el mismo formato de error.
- **Contraseñas fuera de los logs:** el filtro de logging enmascara `Authorization`, `Cookie`, `Set-Cookie`, `Proxy-Authorization` y `X-Api-Key`.
- **Protección contra log injection:** un `X-Request-Id` del cliente solo se acepta si es alfanumérico corto; si no, se genera uno nuevo.
- **Validación en la frontera:** Bean Validation en todos los campos, rechazo de propiedades desconocidas y límite de 100 empleados por lote.
- **Consultas seguras:** JPA Criteria con parámetros (sin SQL concatenado) y escape de comodines en la búsqueda.
- **Imagen Docker endurecida:** multi-stage (solo JRE en runtime), usuario **no root**, `HEALTHCHECK` y límite de memoria relativo al contenedor.
- **Pipeline con seguridad incluida:** build → pruebas + cobertura → escaneo de dependencias (Trivy fs) → imagen → escaneo de la imagen (Trivy image). Falla si hay vulnerabilidades HIGH/CRITICAL con fix disponible.
- `.env` está en `.gitignore`; solo se versiona `.env.example`.

### CI/CD

El pipeline está en **`.github/workflows/ci.yml`** (GitHub Actions) y se ejecuta automáticamente en cada push a `main`, `dev`, `feature/**`, `release/**` y `hotfix/**`, y en cada Pull Request hacia `main` o `dev`:

1. **build-and-test:** compila, corre las pruebas y publica los reportes de pruebas y de cobertura (JaCoCo) como artefactos.
2. **security-scan:** escanea las dependencias, construye la imagen Docker y la escanea. Falla si encuentra vulnerabilidades HIGH/CRITICAL con fix disponible.

Los resultados se ven en la pestaña **Actions** del repositorio en GitHub.

---

## Logs y trazabilidad

La configuración vive en `application.yml` (sin XML):

- Patrón con fecha, nivel, **requestId**, hilo y clase.
- Salida a consola y a archivo (`logs/employees-api.log`) con rotación de 10 MB y 7 días de historial.
- Nivel `DEBUG` para el paquete propio e `INFO` para el resto.

Cada petición deja dos líneas: la entrada con sus **headers** (sensibles enmascarados) y la salida con status y duración:

```
2026-11-26 13:24:47.674  INFO [smoke-1795721087578834700] ... RequestLoggingFilter - --> GET /employees headers={accept=*/*, authorization=****, host=localhost:8081, user-agent=curl/8.16.0, x-request-id=smoke-1795721087578834700}
2026-11-26 13:24:47.690  INFO [smoke-1795721087578834700] ... RequestLoggingFilter - <-- GET /employees status=200 took=16ms
```

---

## Pruebas

```bash
./mvnw clean verify
```

Reporte de cobertura en `target/site/jacoco/index.html`.

| Suite | Tipo | Qué cubre |
|---|---|---|
| `EmployeeServiceImplTest` | Unitaria (Mockito) | Listado, búsqueda por id, alta en lote, actualización parcial, borrado y búsqueda, incluidos los casos `404` en los que **no** se debe escribir nada |
| `EmployeeMapperTest` | Unitaria | `active` por defecto, trim de textos, actualización parcial que no toca lo que no se envió |
| `EmployeeControllerTest` | Web (`@WebMvcTest`) | Cada endpoint y su status: 200/201/204/400/401/404/405/415, formato `dd-MM-yyyy`, objeto vs arreglo, errores con índice del lote, campos desconocidos, requestId y anti log injection |
| `EmployeeRepositoryTest` | JPA (`@DataJpaTest`, H2) | Búsqueda parcial sin distinguir mayúsculas en los cuatro campos de nombre, escape de `%` y `_`, `createdAt` automático |
| `EmployeeApiIntegrationTest` | Integración (`@SpringBootTest`) | Ciclo completo crear → leer → actualizar → buscar → borrar → 404, health y OpenAPI públicos, actuator sensible protegido |
| `H2ConsoleSecurityTest` | Integración (servidor real) | La consola H2 responde `401` sin credenciales y abre con las credenciales de la API |

**Resultado de la última ejecución:** 48 pruebas, 0 fallos (`BUILD SUCCESS`), 87 % de cobertura de instrucciones según JaCoCo.

---

## Documentación del contrato API

El contrato de la API está documentado con **OpenAPI 3** y se puede consultar de tres formas:

| Formato | Ubicación |
|---|---|
| Archivo del contrato (YAML) | [`docs/openapi.yaml`](docs/openapi.yaml), dentro del repositorio |
| Swagger UI (navegable, permite probar los endpoints) | http://localhost:8080/swagger-ui.html, con la aplicación en ejecución |
| Contrato en JSON, generado en vivo | http://localhost:8080/v3/api-docs, con la aplicación en ejecución |

Ahí están todos los endpoints, sus parámetros, los esquemas de request y response con ejemplos, los códigos HTTP posibles, el formato de error y el esquema de seguridad (HTTP Basic). En Swagger UI usa el botón **Authorize** con las credenciales de la API para ejecutar las peticiones.

Como complemento, la colección [`postman/employees-api.postman_collection.json`](postman/employees-api.postman_collection.json) trae peticiones listas para cada endpoint.

---

## Arquitectura y decisiones

```
com.invex.employees
├── controller   → HTTP: rutas, status codes, validación de entrada, OpenAPI
├── service      → reglas de negocio y transacciones (interfaz + implementación)
├── repository   → Spring Data JPA + Specifications para la búsqueda
├── domain       → entidad Employee y enum Sex
├── dto          → contrato de la API (records inmutables)
├── mapper       → traducción DTO ⇄ entidad
├── exception    → excepciones propias + manejador global
├── config       → seguridad, OpenAPI, filtro de logging
└── constants    → rutas, formatos, reglas y mensajes
```

Algunas decisiones y el porqué:

- **Capas delgadas y una responsabilidad por clase (SOLID).** El controlador no sabe de JPA y la entidad nunca sale a la API: el contrato puede cambiar sin tocar la base y viceversa. El controlador depende de la **interfaz** `EmployeeService`, no de la implementación.
- **DTOs como `record`.** Son inmutables y no necesitan boilerplate. La entidad sí usa Lombok porque JPA necesita mutabilidad.
- **Programación funcional donde aporta claridad:** streams para mapear listas, `Optional` para la actualización parcial (`applyIfPresent(value, setter)`) y la búsqueda armada como `Specification` (una lambda) con los campos de nombre recorridos por stream.
- **Mapper manual en lugar de MapStruct.** Con una sola entidad, un procesador de anotaciones extra no se justifica; el mapper se lee en 30 segundos y tiene su propia prueba.
- **Alta en lote con una sola transacción.** O entran todos o ninguno. Así nadie tiene que adivinar qué parte del lote quedó guardada.
- **`edad` como campo libre** (decisión acordada): se valida el rango 18–100, pero no se recalcula a partir de la fecha de nacimiento.
- **Zona horaria fija** (`America/Mexico_City`) en el contenedor, para que `createdAt` signifique lo mismo en una laptop y en Docker.
- **Constantes centralizadas** (`ApiConstants`, `ValidationConstants`, `ErrorMessages`): ni rutas ni mensajes repetidos en el código.

## Git Flow

- `main` → lo que está en producción.
- `dev` → integración.
- `feature/*` → una rama por funcionalidad que sale de `dev` y vuelve por Pull Request.
- `release/*` y `hotfix/*` según Git Flow clásico.

Este trabajo vive en `feature/employee-api`, creada desde `dev`. El pipeline valida cada rama del flujo y cada Pull Request hacia `dev` o `main`.

---

## Entregables y evidencias

| Entregable | Dónde |
|---|---|
| Código fuente | `src/` |
| README de instalación, ejecución y pruebas | este archivo |
| Evidencias (local) | `docs/evidence/api-evidence.md`: 20 llamadas reales con request y response |
| Capturas | `docs/evidence/`: capturas de las pruebas de cada endpoint (archivos `Captura de pantalla 2026-11-26 *.png`), más `swagger-ui.png` y `health-check.png` |
| Contrato de la API | `docs/openapi.yaml` y Swagger UI (ver [Documentación del contrato API](#documentación-del-contrato-api)) |
| Colección de Postman | `postman/employees-api.postman_collection.json` |
| Dockerfile / docker-compose | raíz del proyecto |
| Pipeline CI/CD | `.github/workflows/ci.yml` (GitHub Actions) |

### Cómo usar la colección de Postman

1. En Postman: **Import** → selecciona `postman/employees-api.postman_collection.json`.
2. Abre la colección → pestaña **Variables** y captura los valores de tu entorno:

   | Variable | Qué poner | Ejemplo |
   |---|---|---|
   | `baseUrl` | URL donde corre la API | `http://localhost:8080` |
   | `username` | Usuario de la API | `invex` |
   | `password` | Contraseña de la API | `invex-local-only` |
   | `employeeId` | Id de un empleado que exista | `1` |

3. Guarda los cambios. La autenticación ya está configurada a nivel colección con esas variables, así que todas las peticiones la heredan.
4. Ejecuta primero un **POST** de la carpeta *1. Create*. Revisa el `id` que regresa la respuesta y colócalo en `employeeId` antes de usar los GET, PUT y DELETE por id.

Las peticiones están agrupadas en *Create*, *Read*, *Update*, *Delete* y *Security & Health*, e incluyen los casos de error más comunes (datos inválidos, formato de fecha incorrecto, empleado inexistente, sin credenciales).

---

## Cobertura del ejercicio

| Requisito | Estado |
|---|---|
| 6 endpoints (GET all, GET id, POST uno/varios, PUT total/parcial, DELETE, search parcial) | ✅ |
| Modelo completo, fecha `dd-MM-yyyy`, alta automática, activo/inactivo | ✅ |
| Java 17 · Spring Boot 2.7 · Spring Data JPA/Hibernate | ✅ |
| Base de datos H2 | ✅ |
| JSON request/response | ✅ |
| Excepciones customizadas | ✅ `ApiException`, `EmployeeNotFoundException` + handler global |
| Swagger / OpenAPI | ✅ |
| JUnit 5 + Mockito | ✅ 48 pruebas |
| Logs configurados en YAML | ✅ |
| POO, programación funcional, SOLID, microservicio, validaciones, HTTP status, clean code, constantes, Git Flow | ✅ ver [Arquitectura](#arquitectura-y-decisiones) |
| Extra: Docker | ✅ |
| Extra: Spring Security | ✅ |
| Extra: CI/CD | ✅ GitHub Actions |
| Extra: Health check (actuator) | ✅ |
| Extra: Postman Collection | ✅ |
| Extra: Contrato de API | ✅ `docs/openapi.yaml` + Swagger UI |
| Extra: Registro de headers en logs | ✅ |
