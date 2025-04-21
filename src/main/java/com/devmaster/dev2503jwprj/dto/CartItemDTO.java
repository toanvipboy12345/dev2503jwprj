package com.devmaster.dev2503jwprj.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private String image;
    private Integer quantity;
}