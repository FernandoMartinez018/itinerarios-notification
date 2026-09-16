package com.itinerarios.notification.controller;

import com.itinerarios.notification.dto.NotificationHistoryDto;
import com.itinerarios.notification.repository.NotificationHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint de solo lectura. Notification Service no expone creacion/edicion
 * via HTTP: su unica entrada real es el consumo de eventos de RabbitMQ. Este
 * controller existe para verificar el historial (evidencia de la seccion 96,
 * Nivel 2: "Notification"), no como una API de negocio.
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Consulta de historial de notificaciones (solo lectura)")
public class NotificationHistoryController {

    private final NotificationHistoryRepository repository;

    public NotificationHistoryController(NotificationHistoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Lista todo el historial de notificaciones")
    public ResponseEntity<List<NotificationHistoryDto>> findAll() {
        List<NotificationHistoryDto> history = repository.findAll().stream()
                .map(NotificationHistoryDto::from)
                .toList();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/itinerary/{itineraryId}")
    @Operation(summary = "Lista las notificaciones asociadas a un itinerario")
    public ResponseEntity<List<NotificationHistoryDto>> findByItineraryId(@PathVariable Long itineraryId) {
        List<NotificationHistoryDto> history = repository.findByItineraryId(itineraryId).stream()
                .map(NotificationHistoryDto::from)
                .toList();
        return ResponseEntity.ok(history);
    }
}
