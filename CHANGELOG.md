# Changelog

Todos los cambios relevantes de este proyecto se documentan en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/),
y este proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

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