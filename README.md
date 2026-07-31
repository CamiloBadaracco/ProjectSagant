# ProjectSagant — Servicio de Notificaciones Distribuido

Prueba técnica Java Senior para Sagant: un hub de notificaciones que recibe solicitudes
vía REST, las procesa de forma asincrónica con RabbitMQ, y las despacha por múltiples
canales con reintentos y trazabilidad.

## Cómo levantar el proyecto

Con Docker Compose, desde la raíz del proyecto:

```bash
docker-compose up --build
```

Esto levanta 4 servicios: la aplicación (puerto `8080`), PostgreSQL, RabbitMQ (con su
panel de administración en `http://localhost:15672`, usuario/clave `guest`/`guest`) y
Mailhog (UI para ver los emails "enviados" en `http://localhost:8025`).

Una vez levantado, probá el flujo completo:

```bash
# 1. Obtener un token
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Crear una notificación (reemplazá TOKEN por el valor recibido)
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"recipient":"test@example.com","channel":"EMAIL","subject":"Hola","body":"Probando","priority":"MEDIUM","metadata":{}}'
```

Revisá `http://localhost:8025` para ver el email despachado, o la consola de la app
para el log estructurado (siempre se emite, sea cual sea el canal elegido).

### Para desarrollo local sin Docker

```bash
mvn clean test          # corre con H2, no necesita nada levantado
mvn spring-boot:run      # necesita Postgres/RabbitMQ/Mailhog corriendo en localhost
```

## Precondiciones

- Docker y Docker Compose (v2+)
- Para desarrollo local sin Docker: Java 21 y Maven 3.9+ (o usar el wrapper `./mvnw`)

## Credenciales de prueba

El endpoint `POST /api/auth/token` acepta credenciales fijas (no hay una base de
usuarios real, ver sección de trade-offs):

```json
{ "username": "admin", "password": "admin123" }
```

Devuelve un JWT que se manda como `Authorization: Bearer <token>` en el resto de
los endpoints. El secreto de firma y estas credenciales están hardcodeados en
`application.properties` solo para esta prueba — en un entorno real vendrían de
variables de entorno o un secret manager.

## Decisiones de diseño

- **RabbitMQ como cola.** Desacopla completamente el REST del procesamiento:
  la notificación se persiste y se encola en la misma transacción, y el consumer
  la procesa en su propio hilo. Esto permite que el sistema de despacho esté
  degradado (o directamente caído) sin afectar la disponibilidad del endpoint.

- **Patrón Strategy para los canales.** `NotificationDispatcher` con una
  implementación por canal (`LogNotificationDispatcher`, `HttpNotificationDispatcher`,
  `EmailNotificationDispatcher`), resueltas por un `DispatcherRegistry`. El log
  estructurado corre siempre, además del canal elegido por la notificación, para
  tener trazabilidad completa sin importar el resultado del despacho real.

- **JWT con el soporte nativo de Spring Security 7** (`spring-boot-starter-security-oauth2-resource-server`)
  en vez de una librería como `jjwt`. Evita conflictos de classpath con Jackson 3
  (el JSON por defecto de Spring Boot 4) y es el camino más idiomático dentro del
  ecosistema Spring.

- **Resiliencia en capas**:
    - Si RabbitMQ no está disponible, el `RabbitAdmin` no tumba el arranque de la
      app (`ignoreDeclarationExceptions=true`), y si falla el `publish` al crear una
      notificación, esta igual queda guardada como `PENDING` — el REST responde 201
      en vez de 500.
    - El consumer reintenta el despacho una vez; si vuelve a fallar, la notificación
      queda `FAILED` en la base y el mensaje se rechaza para que RabbitMQ lo mande a
      una dead-letter queue (registro doble: en la app y en el broker).

- **Correlation id de punta a punta.** Un filtro genera (o reutiliza) un
  `X-Correlation-Id` por request HTTP, y ese mismo id viaja en el mensaje de
  RabbitMQ hasta el consumer — así se puede seguir el rastro completo de una
  notificación en los logs (que están en JSON, vía `logstash-logback-encoder`)
  aunque el despacho sea asincrónico.

## Trade-offs y limitaciones

Cosas que dejé afuera a propósito, por el tiempo acotado:

- **Credenciales de auth fijas**, sin base de usuarios real ni hashing de
  contraseña. Para producción, esto sería un proveedor de identidad real (o al
  menos usuarios en base con password hasheado).
- **Un solo reintento** antes de marcar la notificación como fallida. Con más
  tiempo, implementaría backoff exponencial usando colas de retraso en RabbitMQ
  (TTL + DLX escalonados) en vez de un reintento síncrono inmediato.
- **No hay un endpoint para listar/reprocesar notificaciones fallidas** — quedan
  en la base con estado `FAILED` y en la DLQ de RabbitMQ, pero no armé un job ni
  un endpoint para reintentarlas manualmente.
- **Los tests de integración no usan un broker real** (RabbitMQ, mail) — están
  aislados con Mockito o mockeados vía `MockRestServiceServer`/`JavaMailSender`.
  Con más tiempo, sumaría un test end-to-end con Testcontainers para probar el
  flujo completo contra una instancia real de RabbitMQ.
- **Los logs JSON solo van a stdout** — en un entorno real irían a un agregador
  (ELK, Loki, etc.), pero eso queda fuera del alcance de esta prueba.
- **Sin rate limiting** en el endpoint de creación de notificaciones.

## Consideración sobre Jakarta EE

Si este servicio tuviera que deployarse en un servidor de aplicaciones Jakarta EE
como WildFly, en vez de como jar autocontenido con Tomcat embebido, cambiaría
varias cosas:

- **Empaquetado**: pasaría de un fat jar (`java -jar app.jar`) a un `.war`
  desplegado en el servidor, sacando el Tomcat embebido del classpath
  (`spring-boot-starter-webmvc` con `provided` scope para el servlet container) y
  extendiendo `SpringBootServletInitializer`.
- **Datasource**: en vez de que Spring Boot arme el `DataSource` desde
  `application.properties`, usaría un datasource administrado por el propio
  WildFly (definido en su consola o vía CLI), y lo referenciaría por JNDI
  (`spring.datasource.jndi-name`) para que las conexiones y el pool las gestione
  el contenedor, no la aplicación.
- **Mensajería**: WildFly trae ActiveMQ Artemis integrado vía JMS (Jakarta
  Messaging), no RabbitMQ/AMQP. Dos caminos: seguir usando RabbitMQ como un
  servicio externo (como ahora, sin cambios) o migrar `NotificationPublisher`/
  `NotificationConsumer` a la API de JMS para aprovechar los recursos que ya
  administra el servidor — cambiaría la implementación pero no el contrato
  (`NotificationDispatcher` y el resto del dominio quedan iguales).
- **Gestión de transacciones**: pasaría de transacciones locales (`@Transactional`
  de Spring) a JTA, para que la escritura en la base y el envío a la cola de
  mensajes participen de la misma transacción distribuida gestionada por el
  contenedor — algo que ahora resolvemos "a mano" con el `try/catch` alrededor
  del `publish`.
- **Conflictos de classpath**: los application servers Jakarta EE suelen traer
  sus propias versiones de librerías (Jackson, por ejemplo). Como este proyecto
  usa Jackson 3 (`tools.jackson.*`, default de Spring Boot 4), habría que revisar
  con qué versión viene WildFly y probablemente aislar dependencias con
  class-loading modular (los `jboss-deployment-structure.xml` de WildFly)
  para evitar choques de versiones.
- **Seguridad**: podría seguir usando Spring Security dentro del WAR (como
  librería, sin que WildFly lo sepa), o migrar a los mecanismos de seguridad
  propios de Jakarta EE (`jakarta.security.enterprise`) si se quisiera
  aprovechar la gestión de identidades del servidor — pero eso significaría
  reescribir el filtro JWT actual.

En resumen: el dominio (entidades, servicios, dispatchers) no cambiaría casi
nada — es la capa de infraestructura (empaquetado, datasource, mensajería,
transacciones) la que se movería de "administrada por Spring Boot" a
"administrada por el contenedor Jakarta EE".