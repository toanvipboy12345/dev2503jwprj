package com.devmaster.dev2503jwprj.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResultDTO {
    private String idOrders;
    private LocalDateTime ordersDate;
    private BigDecimal totalMoney;
    private String paymentMethodName;
    private String transportMethodName;
    private String nameReceiver;
    private String address;
    private String email;
    private String phone;
    private String paymentStatus; // "SUCCESS" hoặc "FAILED"
    private String paymentFailureReason; // Lý do thất bại (nếu có)

    // Constructor và getters/setters được tạo tự động bởi Lombok
}