package com.early_express.ai_service.ai.infrastructure;

import com.early_express.ai_service.ai.application.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class SlackNotificationService implements NotificationService {
    @Override
    public void notifyShipmentHub(String message) {

    }
}
