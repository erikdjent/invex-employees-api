# API evidence

Generated on 26-11-2026 13:25:21 against `http://localhost:8081` with `scripts/smoke-test.sh`.

### Health check (public)

```http
GET /actuator/health
```

```http
HTTP/1.1 200 
X-Request-Id: smoke-1795721121799398100
Content-Type: application/vnd.spring-boot.actuator.v3+json
Date: Thu, 26 Nov 2026 19:25:21 GMT

{"status":"UP","groups":["liveness","readiness"]}

```

### Request without credentials -> 401

```http
GET /employees
```

```http
HTTP/1.1 401 
X-Request-Id: smoke-1795721121947235500
WWW-Authenticate: Basic realm="employees-api"
Content-Type: application/problem+json
Content-Length: 211
Date: Thu, 26 Nov 2026 19:25:21 GMT

{"title":"Unauthorized","status":401,"detail":"Authentication is required to access this resource","instance":"/employees","requestId":"smoke-1795721121947235500","timestamp":"2026-11-26T13:25:22.0282852-06:00"}

```

### POST several employees in one request -> 201

```http
POST /employees

[
  {"firstName":"Juan","middleName":"Carlos","paternalLastName":"Pérez","maternalLastName":"López","age":30,"sex":"MALE","birthDate":"15-03-1994","position":"Backend Developer"},
  {"firstName":"Ana","paternalLastName":"García","maternalLastName":"Ramírez","age":28,"sex":"FEMALE","birthDate":"02-07-1996","position":"QA Engineer"}
]
```

```http
HTTP/1.1 201 
X-Request-Id: smoke-1795721122088645200
Content-Type: application/json
Date: Thu, 26 Nov 2026 19:25:22 GMT

[{"id":1,"firstName":"Juan","middleName":"Carlos","paternalLastName":"Pérez","maternalLastName":"López","age":30,"sex":"MALE","birthDate":"15-03-1994","position":"Backend Developer","createdAt":"26-11-2026 13:25:22","active":true},{"id":2,"firstName":"Ana","middleName":null,"paternalLastName":"García","maternalLastName":"Ramírez","age":28,"sex":"FEMALE","birthDate":"02-07-1996","position":"QA Engineer","createdAt":"26-11-2026 13:25:22","active":true}]

```

### POST a single employee (plain object) -> 201

```http
POST /employees

{"firstName":"Luis","paternalLastName":"Ramírez","maternalLastName":"Soto","age":41,"sex":"MALE","birthDate":"20-11-1983","position":"Tech Lead","active":false}
```

```http
HTTP/1.1 201 
X-Request-Id: smoke-1795721122973303500
Content-Type: application/json
Date: Thu, 26 Nov 2026 19:25:22 GMT

[{"id":3,"firstName":"Luis","middleName":null,"paternalLastName":"Ramírez","maternalLastName":"Soto","age":41,"sex":"MALE","birthDate":"20-11-1983","position":"Tech Lead","createdAt":"26-11-2026 13:25:23","active":false}]

```

### POST with invalid fields -> 400

```http
POST /employees

[{"firstName":"","paternalLastName":"P3rez","age":15,"birthDate":"15-03-1994","position":"Dev"}]
```

```http
HTTP/1.1 400 
X-Request-Id: smoke-1795721123148350100
Content-Type: application/problem+json
Date: Thu, 26 Nov 2026 19:25:22 GMT

{"title":"Bad Request","status":400,"detail":"One or more fields are invalid","instance":"/employees","requestId":"smoke-1795721123148350100","timestamp":"2026-11-26T13:25:23.2819806-06:00","errors":[{"field":"requests[0].age","message":"must be at least 18"},{"field":"requests[0].firstName","message":"must contain only letters, spaces, apostrophes, dots or hyphens"},{"field":"requests[0].firstName","message":"is required"},{"field":"requests[0].paternalLastName","message":"must contain only letters, spaces, apostrophes, dots or hyphens"},{"field":"requests[0].sex","message":"is required (MALE, FEMALE or OTHER)"}]}

```

### POST with wrong date format -> 400

```http
POST /employees

{"firstName":"Juan","paternalLastName":"Pérez","age":30,"sex":"MALE","birthDate":"1994-03-15","position":"Dev"}
```

```http
HTTP/1.1 400 
X-Request-Id: smoke-1795721123330355500
Content-Type: application/problem+json
Date: Thu, 26 Nov 2026 19:25:22 GMT

{"title":"Bad Request","status":400,"detail":"The request body is malformed or contains invalid values. Dates must use the format dd-MM-yyyy and sex must be MALE, FEMALE or OTHER","instance":"/employees","requestId":"smoke-1795721123330355500","timestamp":"2026-11-26T13:25:23.4762711-06:00"}

```

### GET all employees -> 200

```http
GET /employees
```

```http
HTTP/1.1 200 
X-Request-Id: smoke-1795721123530759100
Content-Type: application/json
Date: Thu, 26 Nov 2026 19:25:22 GMT

[{"id":1,"firstName":"Juan","middleName":"Carlos","paternalLastName":"Pérez","maternalLastName":"López","age":30,"sex":"MALE","birthDate":"15-03-1994","position":"Backend Developer","createdAt":"26-11-2026 13:25:22","active":true},{"id":2,"firstName":"Ana","middleName":null,"paternalLastName":"García","maternalLastName":"Ramírez","age":28,"sex":"FEMALE","birthDate":"02-07-1996","position":"QA Engineer","createdAt":"26-11-2026 13:25:22","active":true},{"id":3,"firstName":"Luis","middleName":null,"paternalLastName":"Ramírez","maternalLastName":"Soto","age":41,"sex":"MALE","birthDate":"20-11-1983","position":"Tech Lead","createdAt":"26-11-2026 13:25:23","active":false}]

```

### GET employee by id -> 200

```http
GET /employees/1
```

```http
HTTP/1.1 200 
X-Request-Id: smoke-1795721123715038000
Content-Type: application/json
Date: Thu, 26 Nov 2026 19:25:22 GMT

{"id":1,"firstName":"Juan","middleName":"Carlos","paternalLastName":"Pérez","maternalLastName":"López","age":30,"sex":"MALE","birthDate":"15-03-1994","position":"Backend Developer","createdAt":"26-11-2026 13:25:22","active":true}

```

### GET non-existing employee -> 404

```http
GET /employees/999
```

```http
HTTP/1.1 404 
X-Request-Id: smoke-1795721123897446300
Content-Type: application/problem+json
Date: Thu, 26 Nov 2026 19:25:24 GMT

{"title":"Not Found","status":404,"detail":"Employee with id 999 was not found","instance":"/employees/999","requestId":"smoke-1795721123897446300","timestamp":"2026-11-26T13:25:24.0438946-06:00"}

```

### GET with non-numeric id -> 400

```http
GET /employees/abc
```

```http
HTTP/1.1 400 
X-Request-Id: smoke-1795721124083711700
Content-Type: application/problem+json
Date: Thu, 26 Nov 2026 19:25:24 GMT

{"title":"Bad Request","status":400,"detail":"Parameter 'id' has an invalid value: 'abc'","instance":"/employees/abc","requestId":"smoke-1795721124083711700","timestamp":"2026-11-26T13:25:24.2194981-06:00"}

```

### Search by partial name 'ram' -> 200

```http
GET /employees/search?name=ram
```

```http
HTTP/1.1 200 
X-Request-Id: smoke-1795721124267043600
Content-Type: application/json
Date: Thu, 26 Nov 2026 19:25:24 GMT

[{"id":2,"firstName":"Ana","middleName":null,"paternalLastName":"García","maternalLastName":"Ramírez","age":28,"sex":"FEMALE","birthDate":"02-07-1996","position":"QA Engineer","createdAt":"26-11-2026 13:25:22","active":true},{"id":3,"firstName":"Luis","middleName":null,"paternalLastName":"Ramírez","maternalLastName":"Soto","age":41,"sex":"MALE","birthDate":"20-11-1983","position":"Tech Lead","createdAt":"26-11-2026 13:25:23","active":false}]

```

### Search without matches -> 200 []

```http
GET /employees/search?name=zzz
```

```http
HTTP/1.1 200 
X-Request-Id: smoke-1795721124460721400
Content-Type: application/json
Date: Thu, 26 Nov 2026 19:25:24 GMT

[]

```

### Search without name -> 400

```http
GET /employees/search
```

```http
HTTP/1.1 400 
X-Request-Id: smoke-1795721124647956900
Content-Type: application/problem+json
Date: Thu, 26 Nov 2026 19:25:24 GMT

{"title":"Bad Request","status":400,"detail":"Required parameter 'name' is missing","instance":"/employees/search","requestId":"smoke-1795721124647956900","timestamp":"2026-11-26T13:25:24.7800592-06:00"}

```

### PUT some fields -> 200

```http
PUT /employees/1

{"position":"Senior Backend Developer","age":31}
```

```http
HTTP/1.1 200 
X-Request-Id: smoke-1795721124814835300
Content-Type: application/json
Date: Thu, 26 Nov 2026 19:25:24 GMT

{"id":1,"firstName":"Juan","middleName":"Carlos","paternalLastName":"Pérez","maternalLastName":"López","age":31,"sex":"MALE","birthDate":"15-03-1994","position":"Senior Backend Developer","createdAt":"26-11-2026 13:25:22","active":true}

```

### PUT all fields -> 200

```http
PUT /employees/2

{"firstName":"Ana","middleName":"Sofía","paternalLastName":"García","maternalLastName":"Ramírez","age":29,"sex":"FEMALE","birthDate":"02-07-1996","position":"QA Lead","active":false}
```

```http
HTTP/1.1 200 
X-Request-Id: smoke-1795721125076006900
Content-Type: application/json
Date: Thu, 26 Nov 2026 19:25:25 GMT

{"id":2,"firstName":"Ana","middleName":"Sofía","paternalLastName":"García","maternalLastName":"Ramírez","age":29,"sex":"FEMALE","birthDate":"02-07-1996","position":"QA Lead","createdAt":"26-11-2026 13:25:22","active":false}

```

### PUT with invalid age -> 400

```http
PUT /employees/1

{"age":150}
```

```http
HTTP/1.1 400 
X-Request-Id: smoke-1795721125314310800
Content-Type: application/problem+json
Date: Thu, 26 Nov 2026 19:25:25 GMT

{"title":"Bad Request","status":400,"detail":"One or more fields are invalid","instance":"/employees/1","requestId":"smoke-1795721125314310800","timestamp":"2026-11-26T13:25:25.4989062-06:00","errors":[{"field":"age","message":"must be at most 100"}]}

```

### PUT non-existing employee -> 404

```http
PUT /employees/999

{"age":40}
```

```http
HTTP/1.1 404 
X-Request-Id: smoke-1795721125545690500
Content-Type: application/problem+json
Date: Thu, 26 Nov 2026 19:25:25 GMT

{"title":"Not Found","status":404,"detail":"Employee with id 999 was not found","instance":"/employees/999","requestId":"smoke-1795721125545690500","timestamp":"2026-11-26T13:25:25.724297-06:00"}

```

### DELETE employee -> 204

```http
DELETE /employees/3
```

```http
HTTP/1.1 204 
X-Request-Id: smoke-1795721125790372500
Date: Thu, 26 Nov 2026 19:25:25 GMT


```

### DELETE again -> 404

```http
DELETE /employees/3
```

```http
HTTP/1.1 404 
X-Request-Id: smoke-1795721126040744100
Content-Type: application/problem+json
Date: Thu, 26 Nov 2026 19:25:25 GMT

{"title":"Not Found","status":404,"detail":"Employee with id 3 was not found","instance":"/employees/3","requestId":"smoke-1795721126040744100","timestamp":"2026-11-26T13:25:26.2218434-06:00"}

```

### GET all after changes -> 200

```http
GET /employees
```

```http
HTTP/1.1 200 
X-Request-Id: smoke-1795721126276841800
Content-Type: application/json
Date: Thu, 26 Nov 2026 19:25:26 GMT

[{"id":1,"firstName":"Juan","middleName":"Carlos","paternalLastName":"Pérez","maternalLastName":"López","age":31,"sex":"MALE","birthDate":"15-03-1994","position":"Senior Backend Developer","createdAt":"26-11-2026 13:25:22","active":true},{"id":2,"firstName":"Ana","middleName":"Sofía","paternalLastName":"García","maternalLastName":"Ramírez","age":29,"sex":"FEMALE","birthDate":"02-07-1996","position":"QA Lead","createdAt":"26-11-2026 13:25:22","active":false}]

```

