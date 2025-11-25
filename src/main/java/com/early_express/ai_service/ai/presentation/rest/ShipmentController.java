package com.early_express.ai_service.ai.presentation.rest;

import com.early_express.ai_service.ai.application.service.ShipmentAiService;
import com.early_express.ai_service.ai.presentation.rest.dto.ShipmentAiRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "📦 Shipment Process API", description = "AI를 이용한 발송 시한 계산 및 Slack 알림 API")
@RestController
@RequestMapping("/api/v1/shipment")
public class ShipmentController {

    private final ShipmentAiService shipmentAiService;

    // 생성자 주입
    public ShipmentController(ShipmentAiService shipmentAiService) {
        this.shipmentAiService = shipmentAiService;
    }

    @Operation(summary = "새 주문 알림 및 최종 발송 시한 계산 요청",
            description = "외부 시스템으로부터 주문 데이터를 받아 AI를 통해 최종 발송 시한을 계산하고 Slack 알림을 보냅니다.")
    @PostMapping(value = "/notify-order", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> notifyNewOrder(@RequestBody ShipmentAiRequest orderRequest) {

        try {
            // 핵심 서비스 호출 (여기서는 ShipmentAiService만 호출한다고 가정)
            // 실제 구현 시에는 Slack 알림까지 포함된 통합 서비스(ShipmentProcessService)를 호출해야 함
            shipmentAiService.processNewOrderForShipment(orderRequest); // 통합 로직을 처리하는 메서드 호출

            return ResponseEntity.ok("AI 기반 발송 시한 계산 및 허브 담당자 알림 처리가 완료되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("요청 오류: " + e.getMessage());

        } catch (Exception e) {
            System.err.println("발송 프로세스 실행 중 예외 발생: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류: 발송 프로세스 처리 중 실패했습니다.");
        }
    }
}
