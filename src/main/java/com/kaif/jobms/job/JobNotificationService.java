package com.kaif.jobms.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobNotificationService {

    // This method will run in a separate thread
    @Async("taskExecutor")
    public void notifyCompany(String jobTitle, Long companyId) {
        try {
            log.info("--- [Thread: {}] Sending notification for: {} ---",
                    Thread.currentThread().getName(), jobTitle);

            Thread.sleep(5000);

            log.info("--- Notification sent ---");
        } catch (InterruptedException e) {
            log.error("Error sending email notification", e);
        }
    }
}