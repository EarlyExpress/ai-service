package com.early_express.ai_service.ai.infrastructure;

import com.early_express.ai_service.ai.application.service.HubRouteEstimator;
import com.early_express.ai_service.ai.application.service.dto.HubDto;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class KakaoHubRouteEstimator implements HubRouteEstimator {

    private final KakaoNavigationHelper helper;


    @Override
    public double[] estimate(List<HubDto> hubs) {

        if (hubs == null || hubs.isEmpty() || hubs.size() < 2) {
            return null;
        }

        HubDto departure = hubs.getFirst(); // 출발지 허브
        HubDto arrival = hubs.getLast(); // 도착지 허브


        // 출발지
        Map<String, Object> origin = Map.of( "name", departure.hubName(), "x", departure.latitude(), "y", departure.longitude());
        // 도착지
       Map<String, Object> destination = Map.of( "name", arrival.hubName(), "x", arrival.latitude() , "y", arrival.longitude());
//

        // 경유지
        List<Map<String, Object>> waypoints = null;
        if (hubs.size() > 2) {
            waypoints = hubs.subList(1, hubs.size() - 1)
                    .stream()
                    .map(hub -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", hub.hubName());
                        item.put("x", hub.latitude());
                        item.put("y", hub.longitude());
                        return item;
                    }).toList();

        }


        return helper.estimate(origin, destination, waypoints);
    }
}
