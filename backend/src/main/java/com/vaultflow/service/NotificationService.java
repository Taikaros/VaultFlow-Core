package com.vaultflow.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendTransactionNotification(String companyId, String type, String message, Map<String, Object> data) {
        messagingTemplate.convertAndSend(
            "/topic/company/" + companyId,
            Map.of("type", type, "message", message, "data", data)
        );
    }
}
