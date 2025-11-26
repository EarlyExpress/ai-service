package com.early_express.ai_service.ai.infrastructure;

import com.early_express.ai_service.ai.application.service.DestinationRouteEstimator;
import com.early_express.ai_service.ai.application.service.HubRouteEstimator;
import com.early_express.ai_service.ai.application.service.TimeCalculateService;
import com.early_express.ai_service.ai.infrastructure.client.HubClient;
import com.early_express.ai_service.ai.application.service.dto.HubDto;
import com.early_express.ai_service.ai.presentation.rest.dto.TimeCalculateRequest;
import com.early_express.ai_service.ai.presentation.rest.dto.TimeCalculateResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeCalculateServiceImpl implements TimeCalculateService {

    private ChatClient chatClient;
    private Resource resource;
    private final ChatClient.Builder builder;
    private final HubClient hubClient;
    private final HubRouteEstimator estimator;
    private final DestinationRouteEstimator routeEstimator;


    @PostConstruct
    public void setup() {
        resource = new ClassPathResource("delivery_deadline_prompt.txt");
        chatClient = builder.build();
    }

    @Override
    public TimeCalculateResponse calculate(TimeCalculateRequest req) {

        List<HubDto> hubs = hubClient.getHubs();
        if (hubs == null || hubs.isEmpty()) return null;

        System.out.println("[응답: ]" + "제대로 들어옵니다.");

        HubDto origin = hubs.stream().filter(hub -> hub.id() == req.originHubId()).findFirst().orElse(null); // 출발 허브
        HubDto destination = hubs.stream().filter(hub -> hub.id() == req.destinationHubId()).findFirst().orElse(null); // 도착 허브
        List<HubDto> routes = hubs.stream().filter(hub -> req.routeHubs().contains(hub.id())).toList();

        if (origin == null || destination == null) {
            System.err.println("오류: 출발지(origin) 또는 도착지(destination) 허브 정보를 찾을 수 없습니다.");
            return null;
        }

        List<HubDto> items = new ArrayList<>(routes);
        items.addFirst(origin);
        items.addLast(destination);


        double[] estimates = estimator.estimate(items);
        if (estimates == null) return null;

        double duration = estimates[1]; // 허브간 이동 총 소요시간


        // 업체 배송 소요시간 및 예상 배송시간
        String address = "%s %s".formatted(req.deliveryAddress(), req.deliveryAddressDetail());
        double[] deliveryEstimates = routeEstimator.estimate(destination, address);
       //double[] deliveryEstimates = {};
        log.info("deliveryEstimates: {}", Arrays.toString(deliveryEstimates));
        double deliveryDuration = 0.0;
        if (deliveryEstimates != null && deliveryEstimates.length >= 2) {
            deliveryDuration = deliveryEstimates[1];
        } else {
            // 배송 시간 추정에 실패했을 경우 로깅 및 0으로 처리 (또는 null 반환 여부는 정책에 따라 결정)
            System.err.println("경고: 배송지까지의 이동 시간 추정에 실패했습니다. (deliveryEstimates is null or invalid) -> 0분 처리");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("origin", "%s(%s)".formatted(origin.hubName(), origin.address()));
        params.put("destination", "%s(%s)".formatted(destination.hubName(), destination.address()));
        params.put("estimateTime1", (int)Math.ceil(duration));
        params.put("estimateTime2", (int)Math.ceil(deliveryDuration));
        params.put("address", address);
        params.put("requestDeliveryDate", req.requestedDeliveryDate() + " " + req.requestedDeliveryTime());
        params.put("totalDuration", (int)Math.ceil(duration + deliveryDuration));
        params.put("orderId", req.orderId());


        String routesStr =  routes.isEmpty() ? "" : routes.stream().map(hub -> "%s / %s".formatted(hub.hubName(), hub.address())).collect(Collectors.joining("\n"));
        params.put("routes", routesStr);

        /**
         * AI가 구해야 하는 것
         * 1. 발송 시한
         * 2. 예상 배송 완료 시간
         * 3. 판단 근거 메세지
         * 4. 계산 성공 여부
         * 5. 에러 메시지(실패 시)
         */
        return chatClient.prompt()
                .user(s -> s.text(resource, StandardCharsets.UTF_8)
                        .params(params)
                )
                .call()
                .entity(TimeCalculateResponse.class);
    }
}
