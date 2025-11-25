package com.early_express.ai_service.ai.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "p_shipment_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime; //주문 시간

    @Column(name = "customer_name", nullable = false)
    private String customerName; //주문자 이름

    @Column(name = "customer_email")
    private String customerEmail; //주문자 이메일

    @ElementCollection
    @CollectionTable(name = "p_item_info", joinColumns = @JoinColumn(name = "request_id"))
    private List<ItemInfoDomain> itemInfos;

//    @Column(name = "delivery_deadline", nullable = false)
//    private LocalDateTime deliveryDeadline; // 납기일자 및 시간*/

    @Column(name = "shipment_origin", nullable = false)
    private String shipmentOrigin; //발송지

    @ElementCollection
    @CollectionTable(name = "p_shipment_waypoint", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "waypoint_location")
    private List<String> waypoints; //경유지목록

    @Column(name = "shipment_destination", nullable = false)
    private String shipmentDestination;

    @Column(name = "delivery_manager_name")
    private String deliveryManagerName;

    @Column(name = "delivery_manager_contact")
    private String deliveryManagerContact;

    @Column(name = "delivery_personnel_work_start")
    private LocalTime personnelWorkStart;

    @Column(name = "delivery_personnel_work_end")
    private LocalTime personnelWorkEnd;

    @Column(name = "ai_final_deadline")
    private LocalDateTime finalShipmentDeadline;

    @Column(name = "ai_estimated_time")
    private LocalDateTime estimatedTime;

    @Builder
    public Shipment(String orderId, LocalDateTime orderTime, String customerName, String customerEmail
            , List<ItemInfoDomain> itemInfos, LocalDateTime deliveryDeadline, String shipmentOrigin
            , List<String> waypoints, String shipmentDestination, String deliveryManagerName
            , String deliveryManagerContact, LocalTime personnelWorkStart, LocalTime personnelWorkEnd) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.itemInfos = itemInfos;
        this.shipmentOrigin = shipmentOrigin;
        this.waypoints = waypoints;
        this.shipmentDestination = shipmentDestination;
        this.deliveryManagerName = deliveryManagerName;
        this.deliveryManagerContact = deliveryManagerContact;
        this.personnelWorkStart = personnelWorkStart;
        this.personnelWorkEnd = personnelWorkEnd;
    }

    // 🌟 [추가] AI 결과를 업데이트하는 메서드 🌟
    public void updateAiResults(LocalDateTime finalDeadline, LocalDateTime estimatedTime) {
        this.finalShipmentDeadline = finalDeadline;
        this.estimatedTime = estimatedTime;
    }


    public void validateShipmentData() {
        // 발송지
        if(shipmentOrigin == null || shipmentOrigin.trim().isEmpty()) {
            throw new IllegalArgumentException("필수 데이터: 발송지(shipmentOrigin)가 누락되었습니다.");
        }

        // 2. 도착지 (shipmentDestination)
        if (shipmentDestination == null || shipmentDestination.trim().isEmpty()) {
            throw new IllegalArgumentException("필수 데이터: 도착지(shipmentDestination)가 누락되었습니다.");
        }

        // 3. 요청 납기일자 및 시간 (requestedDeliveryDeadline)
        if (finalShipmentDeadline == null) {
            throw new IllegalArgumentException("필수 데이터: 요청 납기일자(requestedDeliveryDeadline)가 누락되었습니다.");
        }

        // 4. 배송 담당자 근무 시간 (personnelWorkStart, personnelWorkEnd)
        // AI 계산의 중요한 조건이므로 검증
        if (personnelWorkStart == null || personnelWorkEnd == null) {
            throw new IllegalArgumentException("필수 데이터: 배송 담당자 근무 시작/종료 시간 정보가 누락되었습니다.");
        }

        // 추가 검증: 요청 납기일자가 현재 시간보다 이전인지 확인
        if (finalShipmentDeadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("요청 납기일자는 현재 시간보다 이전일 수 없습니다.");
        }
    }
}
