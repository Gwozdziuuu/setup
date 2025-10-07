package com.mrngwozdz.setup.messaging.sender;

import com.mrngwozdz.setup.messaging.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final RabbitTemplate rabbitTemplate;

    public void send(String routingKey, Object message) {
        log.info("Sending message to exchange {} with routing key {}: {}",
                RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
        log.info("Message sent successfully to exchange");
    }
}