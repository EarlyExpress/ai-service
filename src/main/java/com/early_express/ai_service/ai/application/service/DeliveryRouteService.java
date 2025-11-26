package com.early_express.ai_service.ai.application.service;

import com.early_express.ai_service.ai.presentation.rest.dto.DeliveryRouteRequest;
import com.early_express.ai_service.ai.presentation.rest.dto.DeliveryRouteResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeliveryRouteService {
    private final ChatClient chatClient;
    private final PromptTemplate deliveryRoutePromptTemplate;


    public DeliveryRouteService(

            ChatClient.Builder builder,

            @Value("classpath:delivery_prompt.txt") Resource deliveryRoutePromptResource) {

        this.chatClient = builder.build();

        this.deliveryRoutePromptTemplate = new PromptTemplate(deliveryRoutePromptResource);
    }

    /**
     * 배송 요청 목록을 받아 AI를 통해 최적화된 경로 목록을 반환합니다.
     */
    public List<DeliveryRouteResponse> optimizeRoutes(List<DeliveryRouteRequest> routes) {

        // 1. Prompt Variables 준비
        Map<String, Object> promptVariables = new HashMap<>();

        // 요청 DTO 목록을 AI가 이해할 수 있는 문자열 형식으로 변환합니다.
        String routeRequestsString = routes.stream()
                .map(req -> String.format("{orderId: %s, address: \"%s\"}",
                        req.orderId().toString(),
                        req.address()))
                .collect(Collectors.joining(",\n"));

        promptVariables.put("routes", routeRequestsString);

        // 2. Prompt 생성
        Prompt prompt = deliveryRoutePromptTemplate.create(promptVariables);

        // 3. AI 호출 및 응답 매핑
        return chatClient.prompt(prompt)
                .call()
                // List<T> 타입 매핑을 위해 ParameterizedTypeReference 사용
                .entity(new ParameterizedTypeReference<List<DeliveryRouteResponse>>() {
                });
    }
}
