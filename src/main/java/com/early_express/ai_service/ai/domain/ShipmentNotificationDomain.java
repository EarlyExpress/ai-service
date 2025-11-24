package com.early_express.ai_service.ai.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "p_shipment_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentNotificationDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private ShipmentRequestDomain shipmentRequest;

    @Column(name = "final_shipment_deadline", nullable = false)
    private LocalDateTime finalShipmentDeadline;

    @Column(name = "slack_message_body", columnDefinition = "TEXT", nullable = false)
    private String slackMessageBody;

    @Column(name = "is_notified", nullable = false)
    private boolean isNotified; // 슬랙 알림 처리 여부

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "notification_time")
    private LocalDateTime notificationTime; // 슬랙 알림 발송 시간

    public ShipmentNotificationDomain(ShipmentRequestDomain shipmentRequest, LocalDateTime finalShipmentDeadline
            , String slackMessageBody) {
        this.shipmentRequest = shipmentRequest;
        this.finalShipmentDeadline = finalShipmentDeadline;
        this.slackMessageBody = slackMessageBody;
    }

    public void markAsNotified() {
        this.isNotified = true;
        this.notificationTime = LocalDateTime.now();
    }

    public void failNotification(String reason) {
        this.isNotified = false;
        this.failureReason = reason;
    }

    public void regenerateMessage(LocalDateTime newDeadline, String newMessage) {
        this.finalShipmentDeadline = newDeadline;
        this.slackMessageBody = newMessage;
        this.isNotified = false;
    }
}
