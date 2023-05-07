package com.example.rabbitlargestmarspictureservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${rabbit.queue.name}")
    private String queueName;

    @Value("${rabbit.exchange.name}")
    private String exchangeName;
    @Value("${rabbit.key.name}")
    private String routingKey;

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(routingKey).noargs();
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.nonDurable(queueName).build();
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.directExchange(exchangeName).build();
    }
}
