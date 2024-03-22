package ru.pln.prodconsumers.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class QueueServiceConfig {
    @Bean(autowireCandidate = false)
    @Scope("request")
    public QueueService commonQueue(Integer countThreads, String title){
        return new QueueService(countThreads, title);
    }
    @Bean(autowireCandidate = false)
    @Scope("request")
    public QueueService clerckQueue(Integer countThreads, String title){
        return new QueueService(countThreads, title);
    }
    @Bean(autowireCandidate = false)
    @Scope("request")
    public QueueService bancomatQueue(Integer countThreads, String title){
        return new QueueService(countThreads, title);
    }
}
