package com.devmaster.dev2503jwprj.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "PRODUCT_IMAGES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Size(max = 250, message = "Tên ảnh không được vượt quá 250 ký tự")
    @Column(name = "NAME", length = 250)
    private String name;

    @NotBlank(message = "Đường dẫn ảnh không được để trống")
    @Size(max = 250, message = "Đường dẫn ảnh không được vượt quá 250 ký tự")
    @Column(name = "URLIMG", length = 250)
    private String urlImg;

    @Column(name = "IDPRODUCT")
    private Long idProduct;
}