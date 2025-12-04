package com.kaif.jobms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. Core Pool Size: Always keep 2 threads alive
        executor.setCorePoolSize(2);

        // 2. Max Pool Size: If queue is full, grow to 5 threads
        executor.setMaxPoolSize(5);

        // 3. Queue Capacity: Buffer 500 tasks before growing
        executor.setQueueCapacity(500);

        executor.setThreadNamePrefix("JobNotification-");
        executor.initialize();
        return executor;
    }
}