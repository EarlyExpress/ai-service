package com.early_express.ai_service.ai.presentation.rest.dto;

public record DeliveryRouteResponse(
        int no,
        Long orderId,
        String address,
        double distance,
        double duration,
        double latitude,
        double logitude
) {}
