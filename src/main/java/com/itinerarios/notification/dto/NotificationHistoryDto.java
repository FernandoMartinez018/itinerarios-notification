package com.itinerarios.notification.dto;

import com.itinerarios.notification.entity.NotificationHistory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationHistoryDto(
        Long id,
        UUID eventId,
        Long itineraryId,
        String userName,
        String notificationType,
        String message,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime processedAt,
        String errorMessage
) {
    public static NotificationHistoryDto from(NotificationHistory entity) {
        return new NotificationHistoryDto(
                entity.getId(),
                entity.getEventId(),
                entity.getItineraryId(),
                entity.getUserName(),
                entity.getNotificationType(),
                entity.getMessage(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getProcessedAt(),
                entity.getErrorMessage()
        );
    }
}
