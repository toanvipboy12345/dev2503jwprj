package com.devmaster.dev2503jwprj.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 500, message = "Tên sản phẩm không được vượt quá 500 ký tự")
    @Column(name = "NAME", length = 500)
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @Size(max = 550, message = "Đường dẫn hình ảnh không được vượt quá 550 ký tự")
    @Column(name = "IMAGE", length = 550)
    private String image;

    @Column(name = "IDCATEGORY")
    private Long idCategory;

    @Column(name = "CONTENTS", columnDefinition = "TEXT")
    private String contents;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    @Column(name = "PRICE")
    private BigDecimal price;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    @Column(name = "QUANTITY")
    private Integer quantity;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 160, message = "Slug không được vượt quá 160 ký tự")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang")
    @Column(name = "SLUG", length = 160)
    private String slug;

    @Size(max = 100, message = "Meta Title không được vượt quá 100 ký tự")
    @Column(name = "META_TITLE", length = 100)
    private String metaTitle;

    @Size(max = 500, message = "Meta Keyword không được vượt quá 500 ký tự")
    @Column(name = "META_KEYWORD", length = 500)
    private String metaKeyword;

    @Size(max = 500, message = "Meta Description không được vượt quá 500 ký tự")
    @Column(name = "META_DESCRIPTION", length = 500)
    private String metaDescription;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "ISDELETE")
    private Byte isDelete = 0;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    @Column(name = "ISACTIVE")
    private Byte isActive = 1;
}