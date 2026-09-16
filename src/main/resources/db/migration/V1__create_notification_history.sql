CREATE TABLE notification_history (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL,
    itinerary_id BIGINT NOT NULL,
    user_name VARCHAR(150) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ,
    error_message TEXT
);

CREATE INDEX idx_notification_history_itinerary_id ON notification_history (itinerary_id);
CREATE INDEX idx_notification_history_event_id ON notification_history (event_id);
