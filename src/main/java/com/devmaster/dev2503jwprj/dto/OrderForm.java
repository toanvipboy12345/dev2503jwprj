package com.devmaster.dev2503jwprj.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderForm {
    @NotBlank(message = "Họ tên người nhận không được để trống")
    @Size(max = 250)
    private String nameReceiver;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 500)
    private String address;

    @NotBlank(message = "Email không được để trống")
    @Size(max = 150)
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 50)
    private String phone;

    private String notes;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private Long paymentMethodId;

    @NotNull(message = "Phương thức vận chuyển không được để trống")
    private Long transportMethodId;
}