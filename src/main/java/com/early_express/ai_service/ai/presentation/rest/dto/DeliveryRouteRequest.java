package com.early_express.ai_service.ai.presentation.rest.dto;

import java.util.List;
import java.util.UUID;

public record DeliveryRouteRequest(
        Long orderId,
        String address
) {}
