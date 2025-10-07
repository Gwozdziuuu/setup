package com.mrngwozdz.setup.messaging.sender;

import com.mrngwozdz.setup.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;

    public void send(String routingKey, Object message) {
        log.info("Sending message to exchange {} with routing key {}: {}",
                rabbitMQProperties.getExchangeName(), routingKey, message);
        rabbitTemplate.convertAndSend(rabbitMQProperties.getExchangeName(), routingKey, message);
        log.info("Message sent successfully to exchange");
    }
}