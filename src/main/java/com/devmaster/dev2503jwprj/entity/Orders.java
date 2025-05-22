package com.devmaster.dev2503jwprj.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ORDERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "IDORDERS", length = 10, unique = true)
    private String idOrders;

    @Column(name = "ORDERS_DATE")
    private LocalDateTime ordersDate;

    @ManyToOne
    @JoinColumn(name = "IDCUSTOMER", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "IDPAYMENT", nullable = false)
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "IDTRANSPORT", nullable = false)
    private TransportMethod transportMethod;

    @NotNull(message = "Tổng tiền không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tổng tiền phải lớn hơn 0")
    @Column(name = "TOTAL_MONEY")
    private BigDecimal totalMoney;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @NotBlank(message = "Họ tên người nhận không được để trống")
    @Size(max = 250, message = "Họ tên người nhận không được vượt quá 250 ký tự")
    @Column(name = "NAME_RECEIVER", length = 250)
    private String nameReceiver;

    @NotBlank(message = "Địa chỉ người nhận không được để trống")
    @Size(max = 500, message = "Địa chỉ người nhận không được vượt quá 500 ký tự")
    @Column(name = "ADDRESS", length = 500)
    private String address;

    @NotBlank(message = "Email người nhận không được để trống")
    @Size(max = 150, message = "Email người nhận không được vượt quá 150 ký tự")
    @Email(message = "Email không đúng định dạng")
    @Column(name = "EMAIL", length = 150)
    private String email;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    @Size(max = 50, message = "Số điện thoại người nhận không được vượt quá 50 ký tự")
    @Column(name = "PHONE", length = 50)
    private String phone;

    @Column(name = "ISDELETE")
    private Byte isDelete = 0;

    @NotNull(message = "Trạng thái sử dụng không được để trống")
    @Column(name = "ISACTIVE")
    private Byte isActive = 1;
}