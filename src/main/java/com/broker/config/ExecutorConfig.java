package com.broker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
public class ExecutorConfig {

    @Bean
    public ScheduledExecutorService createScheduledExecutorService(@Value("${scheduler.idle.pool.size}") int idlePoolSize) {
        ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(idlePoolSize);

        scheduledExecutorService.setRemoveOnCancelPolicy(true);

        return scheduledExecutorService;
    }
}
