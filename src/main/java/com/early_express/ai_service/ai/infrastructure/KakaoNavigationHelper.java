package com.early_express.ai_service.ai.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RefreshScope
public class KakaoNavigationHelper {

    @Value("${kakao.apikey}")
    private String apiKey;

    public double[] estimate(Map<String, Object> origin, Map<String, Object> destination, List<Map<String, Object>> waypoints) {

        Map<String, Object> params = new HashMap<>();

        params.put("origin", origin);
        params.put("destination", destination);
        if (waypoints != null && !waypoints.isEmpty()) {
            params.put("waypoints", waypoints);
        }
        System.out.println("[요청]:" + params);
        ResponseEntity<JsonNode> response = RestClient.builder()
                .baseUrl("https://apis-navi.kakaomobility.com/v1/waypoints/directions")
                .build()
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "KakaoAK " + apiKey
                )
                .body(params)
                .retrieve()
                .toEntity(JsonNode.class);

        System.out.println("[응답]: " + "Kakao API 문제가 아닙니다.");

        if (response.getStatusCode().is2xxSuccessful()) {
            double distance = 0.0, duration = 0.0;
            try {
                JsonNode nodes = response.getBody();
                JsonNode routes = nodes.get("routes");
                if (routes != null && routes.isArray()) {
                    for (JsonNode route : routes) {
                        distance += route.get("summary").get("distance").asDouble(0.0);
                        duration += route.get("summary").get("duration").asDouble(0.0);

                    }
                }
                System.out.println("결과값: "+response);
                return new double[] {distance / 1000.0, duration / 60};
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }
}
