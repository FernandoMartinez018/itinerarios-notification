package com.itinerarios.notification.consumer;

import com.itinerarios.notification.config.RabbitMQConfig;
import com.itinerarios.notification.event.ItineraryCreatedEvent;
import com.itinerarios.notification.processor.NotificationProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Unico punto de entrada de mensajeria. Deliberadamente delgado: solo recibe
 * el mensaje y delega toda la logica de negocio en NotificationProcessor,
 * para mantener esa logica independiente del framework de mensajeria
 * (mismo principio de separacion que Controller -> Service en los demas
 * microservicios, seccion 20).
 */
@Component
public class ItineraryEventConsumer {

    private final NotificationProcessor processor;

    public ItineraryEventConsumer(NotificationProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(
            queues = RabbitMQConfig.NOTIFICATION_QUEUE,
            containerFactory = RabbitMQConfig.LISTENER_CONTAINER_FACTORY
    )
    public void onItineraryCreated(ItineraryCreatedEvent event) {
        processor.process(event);
    }
}
