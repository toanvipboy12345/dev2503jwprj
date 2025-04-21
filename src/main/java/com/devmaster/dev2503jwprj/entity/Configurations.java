package com.devmaster.dev2503jwprj.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "CONFIGURATIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configurations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "Tên cấu hình không được để trống")
    @Size(max = 500, message = "Tên cấu hình không được vượt quá 500 ký tự")
    @Column(name = "NAME", length = 500)
    private String name;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ISDELETE")
    private Byte isDelete = 0;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    @Column(name = "ISACTIVE")
    private Byte isActive = 1;
}