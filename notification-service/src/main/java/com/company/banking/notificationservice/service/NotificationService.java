package com.company.banking.notificationservice.service;

import com.company.banking.notificationservice.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendNotification(NotificationRequest request) {
        log.info("--- New Notification ---");
        log.info("To: {}", request.to());
        log.info("Subject: {}", request.subject());
        log.info("Body: {}", request.body());
        log.info("------------------------");
        // In a real application, this would integrate with an email/SMS gateway like Twilio, SendGrid, etc.
    }
}
