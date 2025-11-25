package com.early_express.ai_service.ai.application.service;

/**
 * Slack 알림 서비스의 역할을 정의하는 인터페이스입니다.
 * 실제 구현체는 이 인터페이스를 상속받아 Slack API를 호출하는 로직을 구현해야 합니다.
 */
public interface NotificationService {

    /**
     * 특정 채널 또는 담당자에게 메시지를 전송합니다.
     * @param message 전송할 Slack 메시지 문자열
     */
    void notifyShipmentHub(String message);
}
