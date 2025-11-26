package com.early_express.ai_service.ai.presentation.rest;

import com.early_express.ai_service.ai.application.service.DeliveryRouteService;
import com.early_express.ai_service.ai.presentation.rest.dto.DeliveryRouteRequest;
import com.early_express.ai_service.ai.presentation.rest.dto.DeliveryRouteResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/delivery")
public class DeliveryRouteController {

    private final DeliveryRouteService deliveryRouteService;

    // 💡 생성자 주입
    public DeliveryRouteController(DeliveryRouteService deliveryRouteService) {
        this.deliveryRouteService = deliveryRouteService;
    }

    @PostMapping("/routes")
    public List<DeliveryRouteResponse> routes(@RequestBody List<DeliveryRouteRequest> routes) {
        // 모든 비즈니스 로직을 Service로 위임
        return deliveryRouteService.optimizeRoutes(routes);
    }
}