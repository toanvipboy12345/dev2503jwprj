package com.devmaster.dev2503jwprj.dto;

   import lombok.*;

   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   @Builder
   public class InventoryTransactionDTO {
       private Long productId;
       private Integer quantity;
       private String reason;
   }