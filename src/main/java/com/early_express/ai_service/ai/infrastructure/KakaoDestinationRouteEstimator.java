package com.early_express.ai_service.ai.infrastructure;

import com.early_express.ai_service.ai.application.service.DestinationRouteEstimator;
import com.early_express.ai_service.ai.application.service.dto.HubDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class KakaoDestinationRouteEstimator implements DestinationRouteEstimator {

    @Value("${kakao.apikey}")
    private String apiKey;

    private final KakaoNavigationHelper helper;

    @Override
    public double[] estimate(HubDto hub, String address) {
        // address에 대한 위도 경도 구하기
        RestClient client = RestClient
                .builder()
                .baseUrl("https://dapi.kakao.com/v2/local/search/address.json?query=" + address).build();
        ResponseEntity<JsonNode> res = client.get()
                .header("Authorization", "KakaoAK " + apiKey)
                .retrieve()
                .toEntity(JsonNode.class);
        JsonNode node = res.getBody();
        if (res.getStatusCode().is2xxSuccessful() && node != null) {
            JsonNode docs = node.get("documents");
            if (!docs.isEmpty()) {
                JsonNode addr = docs.get(0).get("address");
                double lat = addr.get("y").asDouble(0.0);// 위도
                double lon = addr.get("x").asDouble(0.0); // 경도


                // 출발지
                Map<String, Object> origin = Map.of("name", hub.hubName(), "x", hub.latitude(), "y", hub.longitude());

                // 도착지
                Map<String, Object> destination = Map.of("name", address, "x", lat, "y", lon);
                return helper.estimate(origin, destination, null);

            }
        }

        return null;
    }
}
