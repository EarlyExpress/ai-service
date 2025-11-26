package com.early_express.ai_service.ai.application.service;

import com.early_express.ai_service.ai.presentation.rest.dto.TimeCalculateRequest;
import com.early_express.ai_service.ai.presentation.rest.dto.TimeCalculateResponse;

public interface TimeCalculateService {
    TimeCalculateResponse calculate(TimeCalculateRequest timeCalculateRequest);
}
