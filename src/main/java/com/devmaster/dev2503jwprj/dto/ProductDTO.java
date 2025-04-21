package com.devmaster.dev2503jwprj.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 500, message = "Tên sản phẩm không được vượt quá 500 ký tự")
    private String name;

    private String description;

    private String notes;

    private String image;

    private Long idCategory;

    private String categoryName;

    private String contents;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer quantity;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 160, message = "Slug không được vượt quá 160 ký tự")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang")
    private String slug;

    @Size(max = 100, message = "Meta Title không được vượt quá 100 ký tự")
    private String metaTitle;

    @Size(max = 500, message = "Meta Keyword không được vượt quá 500 ký tự")
    private String metaKeyword;

    @Size(max = 500, message = "Meta Description không được vượt quá 500 ký tự")
    private String metaDescription;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private Long createdBy;

    private Long updatedBy;

    private Byte isDelete = 0;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    @Min(value = 0, message = "Trạng thái hoạt động chỉ được là 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái hoạt động chỉ được là 0 hoặc 1")
    private Byte isActive = 1;

    private List<ProductConfigDTO> configurations;

    private List<ProductImageDTO> images;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductConfigDTO {
        private Long id;
        private Long idConfig;
        private String configName;
        private String value;
        private String notes; // Thêm trường notes để khớp với form và CONFIGURATIONS
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductImageDTO {
        private Long id;
        private String name;
        private String urlImg;
    }
}