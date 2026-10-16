package com.example.identity.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.identity.configuration.RabbitMQConfiguration;
import com.example.identity.presentation.record.LoginRequestMessage;

/**
 * Sends login requests to RabbitMQ for asynchronous processing.
 * This helps handle high concurrent login traffic by decoupling the login
 * request from the actual processing.
 */
@Component
public class LoginMessageSender {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public LoginMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Sends a login request message to the RabbitMQ queue.
     * 
     * @param message The login request message to send
     * @return The correlation ID for tracking the message
     */
    public String sendLoginRequest(LoginRequestMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.IDENTITY_EXCHANGE,
                RabbitMQConfiguration.LOGIN_ROUTING_KEY,
                message,
                m -> {
                    m.getMessageProperties().setCorrelationId(message.requestId());
                    m.getMessageProperties().setPriority(message.priority());
                    m.getMessageProperties().setHeader("request_id", message.requestId());
                    m.getMessageProperties().setHeader("timestamp", message.timestamp());
                    return m;
                }
        );
        return message.requestId();
    }

    /**
     * Sends a login request with custom priority.
     * Higher priority messages will be processed first.
     * 
     * @param message The login request message
     * @param priority The priority (0-10, where 10 is highest)
     * @return The correlation ID
     */
    public String sendLoginRequestWithPriority(LoginRequestMessage message, int priority) {
        var updatedMessage = LoginRequestMessage.builder()
                .requestId(message.requestId())
                .email(message.email())
                .password(message.password())
                .ipAddress(message.ipAddress())
                .userAgent(message.userAgent())
                .timestamp(message.timestamp())
                .priority(Math.min(Math.max(priority, 0), 10)) // Clamp to 0-10
                .build();
        
        return sendLoginRequest(updatedMessage);
    }

    /**
     * Sends a login request to the retry queue for delayed processing.
     * 
     * @param message The login request message to retry
     * @param delayMillis The delay in milliseconds before retry
     */
    public void sendToRetryQueue(LoginRequestMessage message, long delayMillis) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.IDENTITY_EXCHANGE,
                RabbitMQConfiguration.LOGIN_ROUTING_KEY + ".retry",
                message,
                m -> {
                    m.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                    m.getMessageProperties().setCorrelationId(message.requestId());
                    m.getMessageProperties().setHeader("retry_count", 
                            m.getMessageProperties().getHeader("retry_count") != null 
                                    ? (Integer)m.getMessageProperties().getHeader("retry_count") + 1 
                                    : 1);
                    return m;
                }
        );
    }
}
