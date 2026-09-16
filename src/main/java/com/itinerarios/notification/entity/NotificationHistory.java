package com.itinerarios.notification.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Agregado del Notification Context. `itineraryId` es una referencia lógica
 * a itinerary_db, sin FK real (sección 16 y 102 de la especificación maestra).
 */
@Entity
@Table(name = "notification_history")
public class NotificationHistory {

    public enum Status {
        SENT, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "itinerary_id", nullable = false)
    private Long itineraryId;

    @Column(name = "user_name", nullable = false, length = 150)
    private String userName;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    protected NotificationHistory() {
        // requerido por JPA
    }

    public static NotificationHistory sent(UUID eventId, Long itineraryId, String userName, String message) {
        NotificationHistory history = new NotificationHistory();
        history.eventId = eventId;
        history.itineraryId = itineraryId;
        history.userName = userName;
        history.notificationType = "ITINERARY_CREATED";
        history.message = message;
        history.status = Status.SENT;
        history.processedAt = OffsetDateTime.now();
        return history;
    }

    public static NotificationHistory failed(UUID eventId, Long itineraryId, String userName, String errorMessage) {
        NotificationHistory history = new NotificationHistory();
        history.eventId = eventId;
        history.itineraryId = itineraryId;
        history.userName = userName;
        history.notificationType = "ITINERARY_CREATED";
        history.message = "";
        history.status = Status.FAILED;
        history.processedAt = OffsetDateTime.now();
        history.errorMessage = errorMessage;
        return history;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    // ---- getters ----

    public Long getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Long getItineraryId() {
        return itineraryId;
    }

    public String getUserName() {
        return userName;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
