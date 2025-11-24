package com.early_express.ai_service.ai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemInfoDomain {

    @Column(name = "item_name", nullable = false)
    private String name; //상품명

    @Column(name = "item_quantity", nullable = false)
    private Integer quantitiy; // 수량

    @Column(name = "item_unit")
    private String unit; //단위("개, 박스, 통")

    public ItemInfoDomain increaseQuantity(Integer amount) {
        return new ItemInfoDomain(name, this.quantitiy + amount, this.unit);
    }
}
