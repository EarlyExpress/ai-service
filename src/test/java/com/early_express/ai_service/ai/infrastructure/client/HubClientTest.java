package com.early_express.ai_service.ai.infrastructure.client;

import com.early_express.ai_service.ai.application.service.dto.HubDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class HubClientTest {
    @Autowired
    HubClient client;

    @Test
    void getHubsTest() {
        List<HubDto> hubs = client.getHubs();
        System.out.println("-".repeat(50) + " 허브 목록 " + "-".repeat(50));
        hubs.forEach(System.out::println);
    }
}
