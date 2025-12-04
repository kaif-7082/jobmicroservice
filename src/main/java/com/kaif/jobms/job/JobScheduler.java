package com.kaif.jobms.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobScheduler {

    private final JobRepository jobRepository;

    public JobScheduler(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }


    @Scheduled(cron = "0 0 12 * * MON-FRI")
    public void auditJobCount() {
        long count = jobRepository.count(); // Built-in JPA method
        log.info("--- [Scheduler] Daily Audit: We currently have {} jobs in the database. ---", count);
    }
}