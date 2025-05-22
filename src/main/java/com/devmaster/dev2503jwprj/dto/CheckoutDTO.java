package com.devmaster.dev2503jwprj.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutDTO {

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 250, message = "Tên người nhận không được vượt quá 250 ký tự")
    private String nameReceiver;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email không được vượt quá 150 ký tự")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 50, message = "Số điện thoại không được vượt quá 50 ký tự")
    private String phone;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private Long paymentMethodId;

    @NotNull(message = "Phương thức vận chuyển không được để trống")
    private Long transportMethodId;

    private String notes;
}