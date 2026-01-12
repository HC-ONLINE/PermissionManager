
# PermissionManager — Motor de Autorización Basado en Políticas

**PermissionManager es un sistema de autorización.**
Su responsabilidad es evaluar si una identidad **ya autenticada** puede realizar una acción sobre un recurso determinado, bajo reglas y políticas explícitas.

No gestiona credenciales ni define flujos de login como objetivo principal.

---

## Propósito

Este proyecto demuestra cómo implementar **autorización centralizada y explicable** en una API usando Spring Security:

* Evaluación de decisiones **ALLOW / DENY**
* Control de acceso basado en roles y permisos (RBAC)
* Reglas de negocio adicionales como ownership y restricciones
* Separación clara entre autenticación y autorización

---

## Alcance (importante)

* La autenticación **no es el foco** del sistema.
* Existe un endpoint `/auth/login` **solo como utilidad de desarrollo** para generar identidades de prueba.
* En un entorno real, PermissionManager se integra con un servicio externo de autenticación (por ejemplo, AccessManager).

---

## Modelo de decisión

La autorización se evalúa como una función:

```text
decide(subject, resource, action, context) → ALLOW | DENY
```

* **subject**: identidad (email, roles)
* **resource**: recurso objetivo
* **action**: operación solicitada
* **context**: reglas adicionales (ownership, privilegios)

Cada decisión puede ser explicada y auditada.

---

## Enfoque de autorización

### RBAC + Reglas de Negocio

* Los permisos no están hardcodeados en controladores.
* Los roles agregan permisos explícitos.
* Algunas decisiones requieren validaciones adicionales (ej. ownership).

Ejemplo:

* Un usuario puede leer su propio perfil sin permisos elevados.
* Un administrador puede leer o modificar cualquier perfil.
* Un usuario sin privilegios no puede acceder a recursos ajenos.

---

## Componentes clave

* `@PreAuthorize`: Declaración de políticas en endpoints
* `CustomUserDetailsService`: Resolución de roles y permisos
* Servicios de dominio: validación de ownership y reglas específicas
* Modelo RBAC: `User`, `Role`, `Permission`

---

## Decisiones implementadas

* **ALLOW**: permiso explícito o regla válida
* **DENY**: falta de permisos o violación de reglas
* Cada denegación incluye una **razón clara**, no un 403 genérico

---

## Roles y permisos

| Rol     | Permisos principales                     |
| ------- | ---------------------------------------- |
| USER    | Leer y modificar su propio perfil        |
| SUPPORT | Lectura de perfiles y auditoría          |
| ADMIN   | Gestión completa de usuarios y auditoría |

---

## Endpoints relevantes

| Método | Endpoint     | Política                 |
| ------ | ------------ | ------------------------ |
| GET    | /users/{id}  | Ownership o READ_USER    |
| PUT    | /users/{id}  | Ownership o UPDATE_USER  |
| DELETE | /users/{id}  | DELETE_USER (solo ADMIN) |
| GET    | /admin/audit | READ_AUDIT               |

---

## Limitaciones conocidas

* No hay gestión dinámica de políticas
* No hay cacheo de decisiones
* Auditoría básica (no persistente)
* Se espera una estructura de JWT concreta
* No hay revocación activa de tokens

Estas limitaciones son **intencionales** para mantener el foco en el motor de autorización.

---

## Ejecución

Ejecución (local)

```bash
mvn spring-boot:run
```

Ejecución (Docker)

* **Construir y levantar:**

```bash
docker compose up --build -d
```

> ejecutar desde la raíz del proyecto

* **Login (usuario seed):**
  * Usuario: `admin@example.com`
  * Contraseña: `password`
  * Petición de ejemplo:

  ```bash
  curl -X POST -H "Content-Type: application/json" -d '{"email":"admin@example.com","password":"password"}' http://localhost:8080/api/auth/login
  ```

Nota: la base de datos MySQL se ejecuta en el mismo `docker compose` y la aplicación se conecta internamente; si necesitas acceder al puerto MySQL desde el host, exporta un puerto distinto en `docker-compose.yml`.

### Requisitos

* Java 21
* Base de datos relacional (MySQL o H2)
* Maven 3.6+

---

## Relación con otros sistemas

* **AccessManager**: autentica y emite identidad
* **PermissionManager**: decide permisos
* **Servicios consumidores**: aplican la decisión

La autorización es independiente del mecanismo de autenticación.

---

## Conclusión

PermissionManager no intenta ser un IAM completo.
Su valor está en **decidir correctamente**, **explicar por qué** y **mantener las reglas de acceso fuera de la lógica de negocio**.
Si buscas un motor de autorización claro, centralizado y explicable, este proyecto es un buen punto de partida.
