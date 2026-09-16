package com.itinerarios.notification.event;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Espejo exacto del ItineraryCreatedEvent publicado por itinerary-service y
 * documentado en itinerarios-docs/events/asyncapi.yaml. Cualquier cambio de
 * campo debe sincronizarse en ambos lados (sección 100 de la especificación
 * maestra).
 */
public record ItineraryCreatedEvent(
        UUID eventId,
        String eventType,
        OffsetDateTime occurredAt,
        Long itineraryId,
        String userName,
        String departureAirportId,
        String arrivalAirportId,
        LocalDate travelDate,
        Integer durationMinutes
) {
}
