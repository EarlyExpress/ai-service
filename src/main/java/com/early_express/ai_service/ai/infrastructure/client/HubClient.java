package com.early_express.ai_service.ai.infrastructure.client;

import com.early_express.ai_service.ai.application.service.dto.HubDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("hub-service")
public interface HubClient {
    @GetMapping("web/all/hubs")
    List<HubDto> getHubs();


}
