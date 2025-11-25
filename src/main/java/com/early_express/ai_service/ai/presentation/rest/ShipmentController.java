package com.early_express.ai_service.ai.presentation.rest;

import brave.Response;
import com.early_express.ai_service.ai.application.service.ShipmentAiService;
import com.early_express.ai_service.ai.presentation.rest.dto.ShipmentAiRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
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
    public ResponseEntity<Map<String, String>> notifyNewOrder(@RequestBody ShipmentAiRequest orderRequest) {
        String message;
        HttpStatus status;

        if (orderRequest == null || orderRequest.getOrderId() == null) {
            message = "요청 오류: 주문 데이터가 유효하지 않거나 주문 ID가 누락되었습니다.";
            log.warn(message);
            return ResponseEntity.badRequest().body(Map.of("message", message));
        }

        log.info("주문 ID {}에 대한 AI 처리 요청 시작.", orderRequest.getOrderId());

        try {
            // 핵심 서비스 호출 (여기서는 ShipmentAiService만 호출한다고 가정)
            // 실제 구현 시에는 Slack 알림까지 포함된 통합 서비스(ShipmentProcessService)를 호출해야 함
            shipmentAiService.processNewOrderForShipment(orderRequest); // 통합 로직을 처리하는 메서드 호출

            message = "AI 기반 발송 시한 계산 및 허브 담당자 알림 처리가 완료되었습니다.";
            status = HttpStatus.OK;

            log.info("주문 ID {}에 대한 AI 처리 완료. DB 저장 및 Slack 알림 전송 완료.", orderRequest.getOrderId());

        } catch (IllegalArgumentException e) {
           message = "요청 오류: " + e.getMessage();
           status = HttpStatus.BAD_REQUEST;
           log.warn("주문 ID {} 처리 중 요청 오류 발생: {}", orderRequest.getOrderId(), e.getMessage());

        } catch (RuntimeException e) {
            // ShipmentAiService에서 DB 저장 실패 등 트랜잭션 관련 예외 처리
            message = "발송 프로세스 실행 중 예외 발생: " + e.getMessage();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            log.error("주문 ID {} 처리 중 런타임 오류 발생: {}", orderRequest.getOrderId(), e.getMessage());
        } catch (Exception e) {
            message = "예상치 못한 서버 오류가 발생했습니다: " + e.getMessage();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            log.error("주문 ID {} 처리 중 알 수 없는 오류 발생: {}", orderRequest.getOrderId(), e.getMessage(), e);
        }

        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
