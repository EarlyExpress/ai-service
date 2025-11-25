package com.early_express.ai_service.ai.infrastructure;

import com.early_express.ai_service.ai.domain.Shipment;
import com.early_express.ai_service.ai.presentation.rest.dto.ShipmentAiRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(String orderId);
}
