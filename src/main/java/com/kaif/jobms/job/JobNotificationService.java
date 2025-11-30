package com.kaif.jobms.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j // <--- Creates the 'log' object automatically
@Service
public class JobNotificationService {

    // This method will run in a separate thread
    @Async
    public void notifyCompany(String jobTitle, Long companyId) {
        try {
            // {} is a placeholder that gets replaced by jobTitle efficiently
            log.info("--- [Background Thread] Sending email notification for: {} ---", jobTitle);

            // Simulate a slow email server (5 seconds delay)
            Thread.sleep(5000);

            log.info("--- [Background Thread] Email sent to Company ID {} ---", companyId);
        } catch (InterruptedException e) {
            // This prints the error AND the full stack trace properly
            log.error("Error sending email notification", e);
        }
    }
}