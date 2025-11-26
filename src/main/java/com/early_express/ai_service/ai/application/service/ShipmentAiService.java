package com.early_express.ai_service.ai.application.service;

import com.early_express.ai_service.ai.domain.Shipment;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShipmentAiService {

    private final ChatClient chatClient;
    private final ShipmentRepository shipmentRepository;
    private final NotificationService notificationService;

    //private final ShipmentRepository shipmentRepository;

    @Value("classpath:prompt.txt")
    private Resource shipmentDeadlinePromptResource;

    @Autowired
    public ShipmentAiService(ChatClient.Builder builder, ShipmentRepository shipmentRepository, NotificationService notificationService) {
        this.chatClient = builder.build();
        this.shipmentRepository = shipmentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void processNewOrderForShipment(ShipmentAiRequest request) {

        // AI에게 최종 발송 시한 계산 요청
        ShipmentAiResponse aiResponse = getFinalShipmentDeadlineFromAi(request);

        //AI 계산 결과 추출
        LocalDateTime finalDeadline = aiResponse.getFinalShipmentDeadline(); // 사용하지 않아 주석 처리
        LocalDateTime estimatedTime = aiResponse.getEstimatedTime();
        //System.out.println("finalDeadline = " + aiResponse);

        String orderId = request.getOrderId();

        // TODO: AI 계산 결과(finalDeadline)를 데이터베이스에 저장하거나 후속 처리를 하는 로직 추가 필요
        try {
            // 엔티티 이름: ShipmentEntity 대신 Shipment 클래스를 사용합니다.
            Optional<Shipment> existingShipment = shipmentRepository.findByOrderId(orderId);
            Shipment shipmentToSave;

            if (existingShipment.isPresent()) {
                shipmentToSave = existingShipment.get();
            } else {
                shipmentToSave = Shipment.builder()
                        .orderId(request.getOrderId())
                        .orderTime(request.getOrderTime())
                        .customerName(request.getCustomerName())
                        .customerEmail(request.getCustomerEmail())
                        .itemInfos(request.getItemInfos())
                        .shipmentOrigin(request.getShipmentOrigin())
                        .waypoints(request.getWaypoints())
                        .shipmentDestination(request.getShipmentDestination())
                        .deliveryManagerName(request.getDeliveryManagerName())
                        .deliveryManagerContact(request.getDeliveryManagerContact())
                        .personnelWorkStart(request.getPersonnelWorkStart())
                        .personnelWorkEnd(request.getPersonnelWorkEnd())
                        .build();
            }

            // 4. 엔티티 업데이트: updateAiResults() 메서드 사용 (Shipment 엔티티에 추가된 메서드 가정)
            shipmentToSave.updateAiResults(finalDeadline, estimatedTime);

            // 데이터베이스에 저장
            shipmentRepository.save(shipmentToSave);
            log.info("주문 ID {}의 AI 결과가 DB (Shipment)에 성공적으로 저장되었습니다.", orderId);

        } catch (Exception e) {
            log.error("DB 저장 중 오류 발생 (주문 ID: {}): {}", orderId, e.getMessage());
            // 트랜잭션 롤백 유도
            throw new RuntimeException("DB 저장 실패: " + e.getMessage());
        }

        try {
            // Slack 알림 메시지 생성
            String slackMessage = createSlackMessage(request, finalDeadline, estimatedTime);

            System.out.println(slackMessage);

            // Slack 서비스를 통해 발송 허브 담당자에게 알림 전송
            notificationService.notifyShipmentHub(slackMessage);
            log.info("주문 ID {}에 대한 Slack 알림이 성공적으로 전송되었습니다.", orderId);

        } catch (Exception e) {
            log.warn("Slack 알림 전송 중 오류 발생 (주문 ID: {}): {}", orderId, e.getMessage());
            // 알림 실패는 경고로 처리
        }

    }

    /**
     * 최종 발송 시한 및 예상 도착 시한을 포함한 Slack 알림 메시지를 생성합니다.
     * 이 문자열(slackMessage)이 Slack 담당자에게 전달됩니다.
     */
    private String createSlackMessage(ShipmentAiRequest request, LocalDateTime finalDeadline, LocalDateTime estimatedTime) {

        // 날짜/시간 포맷 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        String deadlineFormatted = finalDeadline.format(formatter);
        String estimatedFormatted = estimatedTime.format(formatter);

        // itemInfos 안전하게 처리
        String itemDescription = request.getItemInfos() != null && !request.getItemInfos().isEmpty()
                ? request.getItemInfos().get(0).getName() + (request.getItemInfos().size() > 1 ? " 외 " + (request.getItemInfos().size() - 1) + "건" : "")
                : "상품 정보 없음";

        StringBuilder message = new StringBuilder();
        message.append("[AI 분석 결과] 긴급 발송 주문 알림\n");
        message.append("---------------------------------------\n");
        message.append("주문 ID:").append(request.getOrderId()).append("\n");
        message.append("상품:").append(itemDescription).append("\n");
        message.append("출발지:").append(request.getShipmentOrigin()).append("\n");
        message.append("도착지:").append(request.getShipmentDestination()).append("\n");
        message.append("최종 발송 시한:").append(deadlineFormatted).append("*\n");
        message.append("예상 도착 시각:").append(estimatedFormatted).append("*\n");
        message.append("배송 요청사항:").append(request.getDeliveryRequest()).append("\n");

        return message.toString();
    }

    // ----------------------------------------------------------------------

    /**
     * AI에게 최종 발송 시한 계산을 요청하고 응답을 받는 내부 메서드 (기존 getFinalShipmentDeadline 로직)
     */
    private ShipmentAiResponse getFinalShipmentDeadlineFromAi(ShipmentAiRequest request) {

        PromptTemplate promptTemplate = new PromptTemplate(shipmentDeadlinePromptResource);
        Map<String, Object> promptVariables = new HashMap<>();

        // 데이터 변환 및 매핑
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

