package com.early_express.ai_service.ai.presentation.rest.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TimeCalculateResponse(
        String orderId,
        LocalDateTime calculatedDepartureDeadline,
        LocalDateTime estimatedDeliveryTime,
        String aiMessage,
        Boolean success,
        String errorMessage,
        Integer hubDeliveryDurationMinutes,
        Integer lastMileDeliveryDurationMinutes,
        Integer totalDeliveryDurationMinutes
) {}
