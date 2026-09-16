# itinerarios-notification

Microservicio del Notification Context - Sistema de Itinerarios Personales.

## Responsabilidad

- Consumir `ItineraryCreatedEvent` publicado por `itinerarios-itinerary-service` via RabbitMQ.
- Simular el envio de una notificacion (sin proveedor de correo real/pago - seccion 104).
- Guardar el historial en `notification_db` (PostgreSQL propio).
- Exponer un endpoint de **solo lectura** para consultar ese historial (usado como
  evidencia de Nivel 2, seccion 96, y por el test E2E de notificaciones).

## Decision de runtime (Nivel 2 vs Nivel 3)

Este servicio corre como una aplicacion Spring Boot normal (consumidor RabbitMQ de
larga duracion), no como AWS Lambda todavia. La seccion 32 de la especificacion
maestra advierte que LocalStack no reproduce automaticamente la integracion
RabbitMQ -> Lambda; el empaquetado como Lambda (imagen de contenedor
`public.ecr.aws/lambda/java:21`, secciones 58-59) se evalua en Nivel 3, al momento del
despliegue real en AWS. Ver `ADR-012-notification-runtime.md` en `itinerarios-docs`.

## Stack

- Java 21, Spring Boot 4.1.1
- Spring AMQP (RabbitMQ)
- PostgreSQL + Flyway

## Diseño interno

```text
RabbitMQ -> ItineraryEventConsumer (@RabbitListener, delgado)
         -> NotificationProcessor (logica de negocio, testeable sin broker)
         -> NotificationHistoryRepository -> notification_db
```

El Consumer solo recibe el mensaje y delega en el Processor - mismo principio de
separacion que Controller -> Service en los demas microservicios (seccion 20). Esto
permite testear `NotificationProcessor` con JUnit/Mockito puro, sin RabbitMQ real.

`RabbitMQConfig` declara su propia queue (`notification.itinerary-created.queue`) y el
binding hacia el exchange `itinerary.events` (declarado tambien, de forma idempotente,
por si Notification Service arranca antes que Itinerary Service).

## Resiliencia (nivel basico para mensajeria)

`default-requeue-rejected: false` + `retry.max-attempts: 2`: un mensaje que falla se
reintenta una vez mas; si vuelve a fallar, se descarta en vez de reencolarse
indefinidamente (seccion 28: "no aplicar Retry indiscriminadamente"). No hay
dead-letter queue configurada todavia - se agrega en Nivel 3 si se prioriza.

## Ejecutar localmente

```bash
export DB_HOST=localhost
export DB_PORT=5435
export DB_NAME=notification_db
export DB_USERNAME=notification_user
export DB_PASSWORD=changeme
export RABBITMQ_HOST=localhost

mvn spring-boot:run
```

## Endpoints

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/notifications` | Historial completo |
| GET | `/api/notifications/itinerary/{itineraryId}` | Historial de un itinerario especifico |

No hay endpoints de escritura: la unica entrada real es el evento de RabbitMQ.
# itinerarios-notification
