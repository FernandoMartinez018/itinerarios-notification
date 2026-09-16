package com.itinerarios.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Notification Service es el consumidor: declara su propia queue y el
 * binding hacia el exchange que publica itinerary-service. El exchange en
 * sí (`itinerary.events`, topic) ya lo declara itinerary-service; declararlo
 * también aquí con los mismos parámetros es idempotente y evita un error de
 * arranque si Notification Service arranca antes que Itinerary Service.
 */
@Configuration
public class RabbitMQConfig {

    public static final String ITINERARY_EVENTS_EXCHANGE = "itinerary.events";
    public static final String ITINERARY_CREATED_ROUTING_KEY = "itinerary.created";
    public static final String NOTIFICATION_QUEUE = "notification.itinerary-created.queue";

    @Bean
    public TopicExchange itineraryEventsExchange() {
        return new TopicExchange(ITINERARY_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange itineraryEventsExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(itineraryEventsExchange)
                .with(ITINERARY_CREATED_ROUTING_KEY);
    }

    public static final String LISTENER_CONTAINER_FACTORY = "notificationListenerContainerFactory";

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean(LISTENER_CONTAINER_FACTORY)
    public SimpleRabbitListenerContainerFactory notificationListenerContainerFactory(
            ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}
