package com.early_express.ai_service.ai.presentation.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data // Getter, Setter, AllArgsConstructor, NoArgsConstructor 포함
public class ShipmentAiResponse {
    // AI가 계산한 최종 발송 시한
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Schema(format = "string")
    private LocalDateTime finalShipmentDeadline;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime estimatedTime;

    // (선택 사항) AI가 정리한 추가 정보나 계산 이유
    private String calculationNote;
}
