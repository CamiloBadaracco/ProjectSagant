# Changelog

Todos los cambios relevantes de este proyecto se documentan en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/),
y este proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### feature/notification-api
### Added
- Endpoint `POST /api/notifications` y `GET /api/notifications/{id}`, protegidos con JWT.
- `NotificationService`: persiste la notificación (PENDING) y la publica en la cola;
  si el broker falla, la notificación queda igual guardada (el REST no se cae).
- `GlobalExceptionHandler` con mensajes de validación descriptivos por campo.
- Tests unitarios del service (incluye el caso de RabbitMQ caído) y de integración
  del endpoint (con y sin token, validación, 404).

### feature/rabbitmq-messaging
### Added
- Configuración de RabbitMQ: exchange, cola principal, dead-letter exchange/queue
  y bindings (`RabbitMQConfig`).
- `NotificationPublisher` para encolar notificaciones, con test unitario (Mockito).
- `RabbitAdmin` configurado con `ignoreDeclarationExceptions=true`: el REST sigue
  funcionando aunque el broker no esté disponible al arrancar.

### Fixed
- Eliminado un `RabbitTemplate` propio que chocaba con el autoconfigurado por
  Spring Boot (usamos el `MessageConverter` como bean para que Boot lo aplique solo).

### feature/security-jwt
### Added
- Autenticación JWT usando Spring Security 7 (spring-boot-starter-security-oauth2-resource-server):
  `JwtConfig`, `JwtService`, `SecurityConfig` y endpoint `POST /api/auth/token`.
- Tests unitarios del ciclo de vida del token y de integración del endpoint de login.

### Fixed
- Ajustes de imports por la modularización de Spring Boot 4.1 (`@DataJpaTest`,
  `@AutoConfigureMockMvc`, `MacAlgorithm`, Jackson 3/`JsonMapper`).

### feature/notification-model
### Added
- Entidad `Notification` con enums `Channel`, `Priority` y `NotificationStatus`.
- Repositorio JPA `NotificationRepository` con test unitario (`@DataJpaTest`).

### feature/proyect-setup
### Added
- Configuración base del proyecto: dependencias de JPA, PostgreSQL, validación,
  seguridad, RabbitMQ, mail y JWT (jjwt).
- Estructura de paquetes: `config`, `security`, `model`, `repository`, `service`,
  `controller`, `dispatch`, `messaging`, `dto`.
- Configuración de `application.properties` para entorno con Docker Compose
  (PostgreSQL, RabbitMQ, Mailhog) y perfil de test con H2 en memoria.

### Removed
- Dependencias `spring-boot-starter-websocket` y `mysql-connector-j`, no utilizadas
  en el diseño del servicio.