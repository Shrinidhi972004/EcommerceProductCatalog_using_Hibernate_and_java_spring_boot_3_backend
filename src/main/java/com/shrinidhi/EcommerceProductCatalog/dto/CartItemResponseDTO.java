package com.shrinidhi.EcommerceProductCatalog.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponseDTO {
    private Long cartItemId;
    private String productName;
    private String productImageUrl;
    private double productPrice;
    private int quantity;
    private double totalPrice;
}
