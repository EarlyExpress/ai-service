package com.early_express.ai_service.ai.presentation.rest.dto;

import com.early_express.ai_service.ai.domain.ItemInfoDomain;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class ShipmentAiRequest {
    // 주문 및 상품 정보
    private String orderId;
    private LocalDateTime orderTime;
    private String customerName;
    private String customerEmail;
    private List<ItemInfoDomain> itemInfos; // ItemInfoDomain 재사용 (DTO로 간주)
    private String deliveryRequest; // 고객의 요청 사항 ("12월 12일 3시까지는 보내주세요!")

    // 배송 경로 정보
    private String shipmentOrigin;
    private List<String> waypoints;
    private String shipmentDestination;

    // 배송 담당자 및 근무 시간
    private String deliveryManagerName;
    private String deliveryManagerContact;
    private LocalTime personnelWorkStart; // 예: 09:00
    private LocalTime personnelWorkEnd;   // 예: 18:00
}
