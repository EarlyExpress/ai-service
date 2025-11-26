package com.early_express.ai_service.ai.application.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HubDto(
        long id,
        String hubName,
        long centralHubId,
        String address,
        double latitude,
        double longitude
) {}
