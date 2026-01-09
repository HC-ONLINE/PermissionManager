# PermissionManager

Sistema de **autorización basado en políticas** desarrollado en Java con Spring Boot y Spring Security.  
El proyecto implementa y compara **dos enfoques de autenticación** para un motor de autorización RBAC:

- Autenticación **stateless** basada en JSON Web Tokens (JWT)
- Autenticación **stateful** basada en sesiones

El objetivo es analizar las **diferencias, ventajas y limitaciones** de cada enfoque dentro del mismo dominio funcional de autorización y control de acceso.

---

## Implementaciones disponibles

Explora las dos implementaciones de autenticación desarrolladas en este proyecto:

### [rbac-jwt](../../tree/rbac-jwt) - Autenticación JWT

Implementación stateless con tokens JWT. Ideal para:

- Arquitecturas de microservicios
- APIs distribuidas
- Escalabilidad horizontal sin configuración adicional

### [rbac-session](../../tree/rbac-session) - Autenticación por Sesión

Implementación stateful con sesiones del servidor. Ideal para:

- Control estricto de acceso con revocación inmediata
- Auditoría completa de sesiones activas
- Aplicaciones monolíticas o con pocos servidores

Cada rama incluye su propio README con:

- Decisiones de diseño explicadas
- Flujo de autenticación y autorización detallado
- Tests de seguridad implementados
- Trade-offs y limitaciones documentadas

---

## Comparación JWT vs Sesión

| Aspecto            | JWT (rbac-jwt)                         | Sesión (rbac-session)                       |
|--------------------|----------------------------------------|---------------------------------------------|
| **Estado**         | Stateless: sin estado en servidor      | Stateful: estado en servidor                |
| **Almacenamiento** | Cliente (token firmado)                | Servidor (sesión HTTP)                      |
| **Escalabilidad**  | Horizontal sin configuración adicional | Requiere sticky sessions o store compartido |
| **Revocación**     | No inmediata (solo por expiración)     | Inmediata (invalidación de sesión)          |
| **Payload**        | Viaja en cada petición (header)        | Solo cookie con ID de sesión                |
| **Validación**     | Verificación criptográfica del token   | Consulta de sesión en memoria/store         |
| **Auditoría**      | Limitada (solo en logs)                | Completa (sesiones activas visibles)        |
| **Casos de uso**   | APIs distribuidas, microservicios      | Aplicaciones monolíticas, control estricto  |

**¿Qué implementación explorar primero?**

- Elige **rbac-jwt** si te interesa arquitecturas distribuidas y escalabilidad stateless
- Elige **rbac-session** si te interesa control de sesiones y revocación inmediata

---

## Qué aprenderás en este repositorio

- **Diferencias prácticas** entre autenticación stateless (JWT) y stateful (sesión)
- Configuración de **Spring Security** para ambos enfoques
- **Trade-offs** reales: escalabilidad vs control, simplicidad vs revocación
- Implementación de **filtros personalizados** (JWT) vs **form login** (sesión)
- **Modelo de autorización RBAC** (Role-Based Access Control) con permisos granulares
- Evaluación de políticas de autorización: `decide(subject, resource, action, context) → ALLOW | DENY`
- **Tests de seguridad** con MockMvc validando flujos de autenticación y autorización
- Cuándo elegir cada enfoque según el contexto del proyecto

Cada rama incluye tests que validan la configuración de seguridad y documentación técnica explicando las decisiones de diseño.

---

## Ramas del proyecto

Navega a la implementación que quieres explorar:

- [**rbac-jwt**](../../tree/rbac-jwt) - Autenticación con JWT (stateless)
- [**rbac-session**](../../tree/rbac-session) - Autenticación con sesión (stateful)

---

## Modelo de autorización

Ambas implementaciones comparten el mismo **motor de autorización basado en políticas**:

```text
decide(subject, resource, action, context) → ALLOW | DENY
```

- **subject**: identidad autenticada (email, roles)
- **resource**: recurso objetivo (usuario, configuración, auditoría)
- **action**: operación solicitada (leer, escribir, eliminar)
- **context**: reglas adicionales (ownership, privilegios especiales)

### Roles y permisos

El sistema implementa tres roles con permisos diferenciados:

- **USER**: Permisos básicos de lectura
- **ADMIN**: Gestión completa de usuarios y configuración
- **SUPPORT**: Acceso de auditoría y soporte técnico

Cada decisión de autorización puede ser explicada y auditada.

---

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.5.9
- Spring Security
- Maven
- JWT (rama `rbac-jwt`)
- Spring Data JPA
- Base de datos H2 (en memoria para desarrollo)

---

## Ejecución

Cada implementación puede ejecutarse de forma independiente desde su rama correspondiente:

```bash
mvn spring-boot:run
```

Para ejecutar tests:

```bash
mvn test
```

---

## Licencia

Este proyecto está licenciado bajo la **Apache License 2.0**.
Consulta el archivo [LICENSE](LICENSE) para más información.

---

## Autor

HC-ONLINE
