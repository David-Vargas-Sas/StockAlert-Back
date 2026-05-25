# StockAlert Backend

API REST para gestion de inventario, ventas, compras, alertas de stock, usuarios, roles y administracion multiempresa.

## Stack

- Java 17
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security con JWT
- PostgreSQL
- H2 para pruebas
- SpringDoc OpenAPI / Swagger UI
- Apache PDFBox para generacion de facturas PDF
- Maven Wrapper

## Requisitos

- JDK 17
- PostgreSQL
- PowerShell, CMD o terminal compatible

No es necesario instalar Maven globalmente. El proyecto incluye Maven Wrapper:

```bash
./mvnw test
```

En Windows:

```powershell
.\mvnw.cmd test
```

## Configuracion

La aplicacion lee sus valores desde variables de entorno, con defaults definidos en `src/main/resources/application.yaml`.

Variables principales:

| Variable | Descripcion | Ejemplo |
| --- | --- | --- |
| `DB_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/stockalert` |
| `DB_USERNAME` | Usuario de base de datos | `postgres` |
| `DB_PASSWORD` | Contrasena de base de datos | `change-me` |
| `JWT_SECRET` | Llave secreta para firmar tokens JWT | `use-a-long-random-secret` |
| `JWT_EXPIRATION_MINUTES` | Minutos de vida del access token | `480` |
| `REFRESH_TOKEN_EXPIRATION_DAYS` | Dias de vida del refresh token | `7` |
| `MAIL_HOST` | Servidor SMTP | `smtp.gmail.com` |
| `MAIL_PORT` | Puerto SMTP | `587` |
| `MAIL_USERNAME` | Usuario SMTP | `user@example.com` |
| `MAIL_PASSWORD` | Contrasena o app password SMTP | `change-me` |
| `BOOTSTRAP_COMPANY_NAME` | Empresa inicial | `StockAlert` |
| `SUPER_ADMIN_USERNAME` | Usuario superadmin inicial | `superadmin` |
| `SUPER_ADMIN_EMAIL` | Email del superadmin inicial | `superadmin@stockalert.local` |
| `SUPER_ADMIN_PASSWORD` | Contrasena inicial del superadmin | `change-me` |

Ejemplo en PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/stockalert"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="change-me"
$env:JWT_SECRET="replace-with-a-long-random-secret"
$env:SUPER_ADMIN_PASSWORD="change-me"
```

## Base de datos

Crear la base de datos local:

```sql
CREATE DATABASE stockalert;
```

La configuracion actual usa:

```yaml
spring.jpa.hibernate.ddl-auto: update
```

Esto permite crear o actualizar tablas durante el desarrollo. Para produccion se recomienda usar migraciones versionadas con Flyway o Liquibase y desactivar la actualizacion automatica del esquema.

## Ejecutar la aplicacion

Desde la raiz del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

Por defecto la API queda disponible en:

```text
http://localhost:8080
```

## Probar

Ejecutar las pruebas automatizadas:

```powershell
.\mvnw.cmd test
```

Las pruebas usan H2 en memoria con la configuracion de `src/test/resources/application.yaml`.

## Documentacion OpenAPI

Con la aplicacion en ejecucion:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

La API usa autenticacion Bearer JWT. Primero se debe iniciar sesion y luego enviar el token en el header:

```http
Authorization: Bearer <access-token>
```

## Autenticacion

Endpoints publicos:

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Inicia sesion y retorna access token y refresh token |
| `POST` | `/api/auth/refresh` | Renueva tokens usando refresh token |
| `POST` | `/api/auth/logout` | Cierra sesion y revoca refresh token |

Ejemplo de login:

```json
{
  "username": "superadmin",
  "password": "change-me"
}
```

## Modulos principales

| Modulo | Ruta base |
| --- | --- |
| Autenticacion | `/api/auth` |
| Empresas | `/api/companies` |
| Usuarios | `/api/users` |
| Roles | `/api/roles` |
| Permisos | `/api/permissions` |
| Productos | `/api/products` |
| Proveedores | `/api/suppliers` |
| Clientes | `/api/customers` |
| Ventas | `/api/sales` |
| Compras | `/api/purchases` |
| Movimientos de inventario | `/api/inventory-movements` |
| Alertas de stock | `/api/alerts` |
| Dashboard | `/api/dashboard` |
| Auditoria | `/api/audit-logs` |
| Logs de sesion | `/api/session-logs` |

## Seguridad y permisos

- La API es stateless y usa JWT.
- `/api/auth/**`, Swagger UI y `/v3/api-docs/**` estan permitidos sin autenticacion.
- `/api/companies/**` requiere rol `SUPER_ADMIN`.
- La mayoria de endpoints restantes requieren autenticacion y permisos por metodo mediante seguridad declarativa.
- CORS permite origenes locales y GitHub Pages configurados en `SecurityConfig`.

## Build

Generar el artefacto:

```powershell
.\mvnw.cmd clean package
```

El JAR se genera en:

```text
target/stockalert-backend-0.0.1-SNAPSHOT.jar
```

Ejecutar el JAR:

```powershell
java -jar target/stockalert-backend-0.0.1-SNAPSHOT.jar
```

## Notas para produccion

Antes de desplegar:

- Usar variables de entorno reales y no guardar secretos en el repositorio.
- Cambiar `JWT_SECRET` por una llave larga, aleatoria y privada.
- Cambiar la contrasena inicial del superadmin.
- Revisar CORS segun el dominio real del frontend.
- Deshabilitar Swagger/OpenAPI si no debe quedar publico.
- Reemplazar `ddl-auto: update` por migraciones de base de datos.
- Configurar backups y monitoreo de PostgreSQL.
