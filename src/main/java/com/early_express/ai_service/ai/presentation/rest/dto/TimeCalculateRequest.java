package com.early_express.ai_service.ai.presentation.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TimeCalculateRequest(
        String orderId,
        long originHubId,
        long destinationHubId,
        List<Long> routeHubs,
        Boolean requiresHubDelivery,
        Double estimatedDistance,
        LocalDate requestedDeliveryDate,
        LocalTime requestedDeliveryTime,
        String deliveryAddress,
        String deliveryAddressDetail,
        int quantity,
        String specialInstructions
) {}
