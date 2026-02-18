package com.backend.situ.service;

import com.backend.situ.entity.Complaint;
import com.backend.situ.enums.ComplaintState;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ComplaintNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComplaintNotificationService.class);

    private final EmailService emailService;
    private final SensitiveDataService sensitiveDataService;

    public ComplaintNotificationService(EmailService emailService, SensitiveDataService sensitiveDataService) {
        this.emailService = emailService;
        this.sensitiveDataService = sensitiveDataService;
    }

    public void notifyStatusChanged(Complaint complaint) {
        String email = sensitiveDataService.decrypt(complaint.getContactEmailEncrypted());
        String trackingToken = sensitiveDataService.decrypt(complaint.getTrackingTokenEncrypted());
        if (email != null && !email.isBlank()) {
            try {
                emailService.sendComplaintStatusChangedEmail(email, trackingToken, complaint.getState());
            } catch (MessagingException ex) {
                LOGGER.error("Could not deliver complaint status email for complaint {}", complaint.getId(), ex);
            }
        }

        // Placeholder for mobile push notifications integration.
        enqueueMobileStatusNotification(trackingToken, complaint.getState());
    }

    private void enqueueMobileStatusNotification(String trackingToken, ComplaintState state) {
        LOGGER.info("Mobile notification pending integration: complaint={} state={}", trackingToken, state);
    }
}
