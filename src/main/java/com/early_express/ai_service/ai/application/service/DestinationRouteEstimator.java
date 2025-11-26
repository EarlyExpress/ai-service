package com.early_express.ai_service.ai.application.service;

import com.early_express.ai_service.ai.application.service.dto.HubDto;

public interface DestinationRouteEstimator {
    double[] estimate(HubDto hub, String address);
}
