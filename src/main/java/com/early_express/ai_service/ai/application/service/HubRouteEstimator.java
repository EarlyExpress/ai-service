package com.early_express.ai_service.ai.application.service;

import com.early_express.ai_service.ai.application.service.dto.HubDto;

import java.util.List;

public interface HubRouteEstimator {
    double[] estimate(List<HubDto> hubs);
}
