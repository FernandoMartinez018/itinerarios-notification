package com.itinerarios.notification.processor;

import com.itinerarios.notification.entity.NotificationHistory;
import com.itinerarios.notification.event.ItineraryCreatedEvent;
import com.itinerarios.notification.repository.NotificationHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationProcessorTest {

    @Mock
    private NotificationHistoryRepository repository;

    @InjectMocks
    private NotificationProcessor processor;

    private ItineraryCreatedEvent sampleEvent() {
        return new ItineraryCreatedEvent(
                UUID.randomUUID(),
                "ItineraryCreatedEvent",
                OffsetDateTime.now(),
                123L,
                "Juan Perez",
                "BOG",
                "MDE",
                LocalDate.now().plusDays(5),
                60
        );
    }

    @Test
    void process_guardaHistorialConEstadoSentCuandoTodoSaleBien() {
        ItineraryCreatedEvent event = sampleEvent();

        processor.process(event);

        ArgumentCaptor<NotificationHistory> captor = ArgumentCaptor.forClass(NotificationHistory.class);
        verify(repository, times(1)).save(captor.capture());

        NotificationHistory saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationHistory.Status.SENT);
        assertThat(saved.getItineraryId()).isEqualTo(123L);
        assertThat(saved.getUserName()).isEqualTo("Juan Perez");
        assertThat(saved.getMessage()).contains("BOG").contains("MDE");
    }

    @Test
    void process_guardaHistorialConEstadoFailedCuandoLaPersistenciaFalla() {
        ItineraryCreatedEvent event = sampleEvent();

        // Primera llamada a save() (dentro del try) falla; el catch intenta
        // guardar un registro FAILED, que sí se permite.
        when(repository.save(any(NotificationHistory.class)))
                .thenThrow(new RuntimeException("DB caída"))
                .thenAnswer(inv -> inv.getArgument(0));

        processor.process(event);

        ArgumentCaptor<NotificationHistory> captor = ArgumentCaptor.forClass(NotificationHistory.class);
        verify(repository, times(2)).save(captor.capture());

        NotificationHistory failedRecord = captor.getAllValues().get(1);
        assertThat(failedRecord.getStatus()).isEqualTo(NotificationHistory.Status.FAILED);
        assertThat(failedRecord.getErrorMessage()).contains("DB caída");
    }
}
