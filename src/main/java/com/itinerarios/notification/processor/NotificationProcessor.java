package com.itinerarios.notification.processor;

import com.itinerarios.notification.entity.NotificationHistory;
import com.itinerarios.notification.event.ItineraryCreatedEvent;
import com.itinerarios.notification.repository.NotificationHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * Lógica de negocio de procesar un ItineraryCreatedEvent, separada del
 * consumer de mensajería (sección 30: "Notification Processor"). Esta
 * separación permite testear la lógica de negocio con JUnit/Mockito puro,
 * sin necesidad de un broker RabbitMQ real ni de @RabbitListener.
 *
 * La notificación es SIMULADA (sección 30/104): no hay integración con un
 * proveedor de correo real ni pago.
 */
@Component
public class NotificationProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationProcessor.class);

    private final NotificationHistoryRepository repository;

    public NotificationProcessor(NotificationHistoryRepository repository) {
        this.repository = repository;
    }

    public void process(ItineraryCreatedEvent event) {
        // El Outbox entrega "al menos una vez" (ADR-008): un evento ya notificado se ignora.
        if (repository.existsByEventIdAndStatus(event.eventId(), NotificationHistory.Status.SENT)) {
            log.info("Evento {} ya notificado; duplicado ignorado", event.eventId());
            return;
        }
        try {
            String message = buildMessage(event);
            simulateNotificationDelivery(event, message);

            NotificationHistory history = NotificationHistory.sent(
                    event.eventId(), event.itineraryId(), event.userName(), message);
            repository.save(history);
        } catch (DataIntegrityViolationException ex) {
            // Carrera entre dos entregas del mismo evento: el indice unico (V2) ya la resolvio.
            log.info("Evento {} ya notificado por otra entrega concurrente; duplicado ignorado", event.eventId());
        } catch (Exception ex) {
            log.error("Failed to process ItineraryCreatedEvent for itinerary {}", event.itineraryId(), ex);
            NotificationHistory history = NotificationHistory.failed(
                    event.eventId(), event.itineraryId(), event.userName(), ex.getMessage());
            repository.save(history);
        }
    }

    private void simulateNotificationDelivery(ItineraryCreatedEvent event, String message) {
        // Punto de extensión para un proveedor real (ej. envío de email) en
        // una fase posterior. Por ahora, solo se registra en el log.
        log.info("Simulando envío de notificación a {}: {}", event.userName(), message);
    }

    private String buildMessage(ItineraryCreatedEvent event) {
        return "Tu itinerario de %s a %s para el %s fue creado exitosamente."
                .formatted(event.departureAirportId(), event.arrivalAirportId(), event.travelDate());
    }
}
