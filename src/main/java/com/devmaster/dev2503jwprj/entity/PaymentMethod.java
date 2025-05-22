package com.devmaster.dev2503jwprj.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENT_METHOD")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "Tên phương thức không được để trống")
    @Size(max = 250, message = "Tên phương thức không được vượt quá 250 ký tự")
    @Column(name = "NAME", length = 250)
    private String name;

    @Column(name = "NOTES", columnDefinition = "TEXT") // Thay NTEXT bằng TEXT
    private String notes;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    @Column(name = "ISDELETE")
    private Byte isDelete = 0;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    @Column(name = "ISACTIVE")
    private Byte isActive = 1;
}