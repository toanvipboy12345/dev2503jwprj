
package com.devmaster.dev2503jwprj.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ORDERS_DETAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "IDORD", nullable = false)
    private Orders order;

    @ManyToOne
    @JoinColumn(name = "IDPRODUCT", nullable = false)
    private Product product;

    @NotNull(message = "Giá mua không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá mua phải lớn hơn 0")
    @Column(name = "PRICE")
    private BigDecimal price;

    @NotNull(message = "Số lượng mua không được để trống")
    @Min(value = 1, message = "Số lượng mua phải lớn hơn 0")
    @Column(name = "QTY")
    private Integer qty;

    @NotNull(message = "Tổng thành tiền không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tổng thành tiền phải lớn hơn 0")
    @Column(name = "TOTAL")
    private BigDecimal total;

    @NotNull(message = "Số lượng trả lại không được để trống")
    @Min(value = 0, message = "Số lượng trả lại phải lớn hơn hoặc bằng 0")
    @Column(name = "RETURN_QTY")
    private Integer returnQty = 0;
}