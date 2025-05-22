package com.devmaster.dev2503jwprj.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "INVENTORY_TRANSACTION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Column(name = "TRANSACTION_TYPE")
    private String transactionType; // STOCK_IN, STOCK_OUT, ADJUSTMENT

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "CREATED_BY")
    private Long createdBy;
}