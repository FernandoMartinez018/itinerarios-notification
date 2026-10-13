-- Idempotencia del consumidor (ADR-015).
-- El Transactional Outbox (ADR-008) garantiza entrega "al menos una vez": el mismo
-- ItineraryCreatedEvent puede llegar dos veces (p. ej. si el OutboxPublisher publica
-- y cae antes de marcar PUBLISHED). Este índice impide registrar dos notificaciones
-- SENT para el mismo evento. Es parcial (solo status = 'SENT') para que los intentos
-- FAILED sigan quedando en el historial como evidencia.
CREATE UNIQUE INDEX ux_notification_history_event_id_sent
    ON notification_history (event_id)
    WHERE status = 'SENT';
