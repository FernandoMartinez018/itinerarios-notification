package com.itinerarios.notification.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.itinerarios.notification.NotificationServiceApplication;
import com.itinerarios.notification.event.ItineraryCreatedEvent;
import com.itinerarios.notification.processor.NotificationProcessor;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * SendNotificationFunction (secciones 31, 58, 89): entrada AWS Lambda / LocalStack.
 * Delega en el mismo NotificationProcessor que usa el consumer RabbitMQ, asi que la
 * logica de negocio es identica en local y en cloud (seccion 60); solo cambia el transporte.
 *
 * Se usa RequestStreamHandler para deserializar con el JsonMapper de Spring (Jackson 3),
 * el mismo que lee los mensajes de RabbitMQ, en vez del serializador propio del runtime.
 */
public class NotificationLambdaHandler implements RequestStreamHandler {

    // Reutilizado entre invocaciones del mismo contenedor: Spring arranca una vez por cold start.
    static ConfigurableApplicationContext context;

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context lambdaContext) throws IOException {
        ConfigurableApplicationContext ctx = context();
        ItineraryCreatedEvent event = ctx.getBean(ObjectMapper.class).readValue(input, ItineraryCreatedEvent.class);
        ctx.getBean(NotificationProcessor.class).process(event);
        output.write(("{\"status\":\"PROCESSED\",\"eventId\":\"" + event.eventId() + "\"}")
                .getBytes(StandardCharsets.UTF_8));
    }

    private static synchronized ConfigurableApplicationContext context() {
        if (context == null) {
            context = new SpringApplicationBuilder(NotificationServiceApplication.class)
                    .web(WebApplicationType.NONE)
                    .profiles("lambda")
                    .run();
        }
        return context;
    }
}
