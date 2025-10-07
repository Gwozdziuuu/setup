package com.mrngwozdz.setup.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class RabbitMQProperties {

    private String exchangeName = "setup.direct.exchange";

    private Queue order = new Queue("setup.order.queue", "order");
    private Queue notification = new Queue("setup.notification.queue", "notification");
    private Queue audit = new Queue("setup.audit.queue", "audit");

    @Data
    public static class Queue {
        private String name;
        private String routingKey;

        public Queue(String name, String routingKey) {
            this.name = name;
            this.routingKey = routingKey;
        }

    }
}