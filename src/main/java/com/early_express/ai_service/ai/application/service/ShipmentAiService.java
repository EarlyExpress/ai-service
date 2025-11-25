package com.early_express.ai_service.ai.application.service;

import com.early_express.ai_service.ai.infrastructure.ShipmentRepository;
import com.early_express.ai_service.ai.presentation.rest.dto.ShipmentAiRequest;
import com.early_express.ai_service.ai.presentation.rest.dto.ShipmentAiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShipmentAiService {

    private final ChatClient chatClient;
    //private final SlackNotificationService slackService; //이후에 추가

    //private final ShipmentRepository shipmentRepository;

    @Value("classpath:prompt.txt")
    private Resource shipmentDeadlinePromptResource;

    @Autowired
    public ShipmentAiService(ChatClient.Builder builder, ShipmentRepository shipmentRepository) {
        this.chatClient = builder.build();
    }

    @Transactional
    public void processNewOrderForShipment(ShipmentAiRequest request) {

        // AI에게 최종 발송 시한 계산 요청
        ShipmentAiResponse aiResponse = getFinalShipmentDeadlineFromAi(request);

        //AI 계산 결과 추출
        //LocalDateTime finalDeadline = aiResponse.getFinalShipmentDeadline(); // 사용하지 않아 주석 처리
        System.out.println("finalDeadline = " + aiResponse);

        // TODO: AI 계산 결과(finalDeadline)를 데이터베이스에 저장하거나 후속 처리를 하는 로직 추가 필요
    }

    // ----------------------------------------------------------------------

    /**
     * AI에게 최종 발송 시한 계산을 요청하고 응답을 받는 내부 메서드 (기존 getFinalShipmentDeadline 로직)
     */
    private ShipmentAiResponse getFinalShipmentDeadlineFromAi(ShipmentAiRequest request) {

        PromptTemplate promptTemplate = new PromptTemplate(shipmentDeadlinePromptResource);
        Map<String, Object> promptVariables = new HashMap<>();

        // 데이터 변환 및 매핑 (이전 답변에서 작성된 로직)
        String itemInfosString = request.getItemInfos() != null ?
                request.getItemInfos().stream()
                        .map(item -> item.getName() + " " + item.getQuantitiy() + item.getUnit())
                        .collect(Collectors.joining(", "))
                : "상품 정보 없음"; // null일 경우 AI에게 전달할 메시지

        String waypointsString = request.getWaypoints() != null && !request.getWaypoints().isEmpty()
                ? String.join(", ", request.getWaypoints())
                : "없음";

        // DTO 필드와 프롬프트 변수 매핑
        promptVariables.put("orderId", request.getOrderId());

        promptVariables.put("orderTime",
                request.getOrderTime() != null ? request.getOrderTime().toString() : "주문 시간 정보 없음");

        promptVariables.put("customerName", request.getCustomerName());
        promptVariables.put("customerEmail", request.getCustomerEmail());
        promptVariables.put("itemInfos", itemInfosString);
        promptVariables.put("deliveryRequest", request.getDeliveryRequest());
        promptVariables.put("shipmentOrigin", request.getShipmentOrigin());
        promptVariables.put("waypoints", waypointsString);
        promptVariables.put("shipmentDestination", request.getShipmentDestination());
        promptVariables.put("deliveryManagerName", request.getDeliveryManagerName());
        promptVariables.put("deliveryManagerContact", request.getDeliveryManagerContact());

        promptVariables.put("estimatedTime",
                request.getEstimatedTime() != null ? request.getEstimatedTime().toString() : "예상 도착 시간 정보 없음");

        promptVariables.put("personnelWorkStart",
                request.getPersonnelWorkStart() != null ? request.getPersonnelWorkStart().toString() : "09:00:00");


        promptVariables.put("personnelWorkEnd",
                request.getPersonnelWorkEnd() != null ? request.getPersonnelWorkEnd().toString() : "18:00:00");

        Prompt prompt = promptTemplate.create(promptVariables);
       /* String response = chatClient.prompt(prompt)
                .call()
                .content();*/

        return chatClient.prompt(prompt).call().entity(ShipmentAiResponse.class);
    }
}
    /**
     * Slack 메시지 본문을 생성합니다. (현재 사용하지 않음)
     *//*
    private String createSlackMessage(ShipmentAiRequest req, LocalDateTime deadline) {
        // 요구사항에 맞춰 메시지 본문 생성
        return String.format(
            "*🔔 [긴급] 최종 발송 시한 알림*\n\n*최종 발송 시한:* %s\n(이 시간까지 발송해야 납기일자를 맞출 수 있습니다.)\n---\n" +
            "*주문 번호:* %s\n*주문 시간:* %s\n*주문자 정보:* %s / %s\n*상품 정보:* %s\n*요청 사항:* %s\n" +
            "*발송지:* %s\n*경유지:* %s\n*도착지:* %s\n*배송 담당자:* %s / %s\n",
            deadline.toString(),
            req.getOrderId(), req.getOrderTime().toString(),
            req.getCustomerName(), req.getCustomerEmail(),
            req.getItemInfos().stream().map(i -> i.getName() + " " + i.getQuantitiy() + i.getUnit()).collect(Collectors.joining(", ")),
            req.getDeliveryRequest(),
            req.getShipmentOrigin(),
            req.getWaypoints() != null ? String.join(", ", req.getWaypoints()) : "없음",
            req.getShipmentDestination(),
            req.getDeliveryManagerName(), req.getDeliveryManagerContact());
    }*/

