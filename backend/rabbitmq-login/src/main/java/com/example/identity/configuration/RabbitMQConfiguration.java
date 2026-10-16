package com.example.identity.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfiguration {

    // Exchange name for identity module
    public static final String IDENTITY_EXCHANGE = "identity.exchange";
    
    // Queue for handling high concurrent login traffic
    public static final String LOGIN_QUEUE = "identity.login.queue";
    
    // Routing key for login messages
    public static final String LOGIN_ROUTING_KEY = "identity.login.routingkey";
    
    // Dead letter exchange for failed login attempts
    public static final String LOGIN_DEAD_LETTER_EXCHANGE = "identity.login.deadletter.exchange";
    
    // Dead letter queue for failed login attempts
    public static final String LOGIN_DEAD_LETTER_QUEUE = "identity.login.deadletter.queue";
    
    // Retry queue for login with delayed processing
    public static final String LOGIN_RETRY_QUEUE = "identity.login.retry.queue";
    
    // Prefetch count to control how many messages a consumer can handle at once
    @Value("${rabbitmq.login.prefetch.count:10}")
    private int prefetchCount;
    
    // Concurrent consumers for login queue
    @Value("${rabbitmq.login.concurrent.consumers:5}")
    private int concurrentConsumers;
    
    // Max concurrent consumers for login queue
    @Value("${rabbitmq.login.max.concurrent.consumers:10}")
    private int maxConcurrentConsumers;

    @Bean
    public DirectExchange identityExchange() {
        return new DirectExchange(IDENTITY_EXCHANGE, true, false);
    }

    @Bean
    public Queue loginQueue() {
        return QueueBuilder.durable(LOGIN_QUEUE)
                .withArgument("x-dead-letter-exchange", LOGIN_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", LOGIN_ROUTING_KEY)
                .withArgument("x-max-priority", 10) // Support priority queue
                .build();
    }

    @Bean
    public Binding loginBinding(Queue loginQueue, DirectExchange identityExchange) {
        return BindingBuilder.bind(loginQueue).to(identityExchange).with(LOGIN_ROUTING_KEY);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(LOGIN_DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(LOGIN_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(LOGIN_ROUTING_KEY);
    }

    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable(LOGIN_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", IDENTITY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", LOGIN_ROUTING_KEY)
                .withArgument("x-message-ttl", 5000) // 5 seconds retry delay
                .build();
    }

    @Bean
    public Binding retryBinding(Queue retryQueue, DirectExchange identityExchange) {
        return BindingBuilder.bind(retryQueue).to(identityExchange).with(LOGIN_ROUTING_KEY + ".retry");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(IDENTITY_EXCHANGE);
        rabbitTemplate.setRoutingKey(LOGIN_ROUTING_KEY);
        rabbitTemplate.setChannelTransacted(true);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(prefetchCount);
        factory.setConcurrentConsumers(concurrentConsumers);
        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
        factory.setDefaultRequeueRejected(false);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }
}
