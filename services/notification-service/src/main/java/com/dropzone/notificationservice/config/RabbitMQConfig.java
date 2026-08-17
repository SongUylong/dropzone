package com.dropzone.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_JOBS = "dropzone.jobs.exchange";

    public static final String QUEUE_TICKET_GENERATION = "ticket-generation.queue";
    public static final String QUEUE_EMAIL = "email.queue";
    public static final String QUEUE_SMS = "sms.queue";

    public static final String ROUTING_KEY_TICKET = "job.ticket-generation";
    public static final String ROUTING_KEY_EMAIL = "job.email";
    public static final String ROUTING_KEY_SMS = "job.sms";

    @Bean
    public TopicExchange jobsExchange() {
        return new TopicExchange(EXCHANGE_JOBS, true, false);
    }

    @Bean
    public Queue ticketGenerationQueue() {
        return QueueBuilder.durable(QUEUE_TICKET_GENERATION).build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL).build();
    }

    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(QUEUE_SMS).build();
    }

    @Bean
    public Binding ticketBinding(Queue ticketGenerationQueue, TopicExchange jobsExchange) {
        return BindingBuilder.bind(ticketGenerationQueue).to(jobsExchange).with(ROUTING_KEY_TICKET);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange jobsExchange) {
        return BindingBuilder.bind(emailQueue).to(jobsExchange).with(ROUTING_KEY_EMAIL);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, TopicExchange jobsExchange) {
        return BindingBuilder.bind(smsQueue).to(jobsExchange).with(ROUTING_KEY_SMS);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
