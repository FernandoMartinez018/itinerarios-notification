package com.itinerarios.notification.lambda;

import com.itinerarios.notification.event.ItineraryCreatedEvent;
import com.itinerarios.notification.processor.NotificationProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationLambdaHandlerTest {

    @AfterEach
    void tearDown() {
        NotificationLambdaHandler.context = null;
    }

    @Test
    void handleRequest_deserializaElEventoYDelegaEnElProcessor() throws Exception {
        NotificationProcessor processor = mock(NotificationProcessor.class);
        ConfigurableApplicationContext ctx = mock(ConfigurableApplicationContext.class);
        when(ctx.getBean(ObjectMapper.class)).thenReturn(JsonMapper.builder().build());
        when(ctx.getBean(NotificationProcessor.class)).thenReturn(processor);
        NotificationLambdaHandler.context = ctx;

        // Payload con el formato del contrato AsyncAPI (seccion 25).
        String payload = """
                {"eventId":"7b1f0c1e-1111-4a2b-9c3d-000000000001","eventType":"ItineraryCreatedEvent",
                 "occurredAt":"2026-10-09T20:00:00Z","itineraryId":123,"userName":"Juan Perez",
                 "departureAirportId":"BOG","arrivalAirportId":"MDE","travelDate":"2026-10-20","durationMinutes":60}""";
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new NotificationLambdaHandler().handleRequest(
                new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)), out, null);

        ArgumentCaptor<ItineraryCreatedEvent> captor = ArgumentCaptor.forClass(ItineraryCreatedEvent.class);
        verify(processor).process(captor.capture());
        assertThat(captor.getValue().itineraryId()).isEqualTo(123L);
        assertThat(captor.getValue().travelDate()).isEqualTo(LocalDate.of(2026, 10, 20));
        assertThat(out.toString(StandardCharsets.UTF_8)).contains("PROCESSED");
    }
}
