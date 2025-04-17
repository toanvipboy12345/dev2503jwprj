package com.devmaster.dev2503jwprj.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "CATEGORY")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 500, message = "Tên danh mục không được vượt quá 500 ký tự")
    @Column(name = "NAME", length = 500)
    private String name;

    @Column(name = "NOTES", columnDefinition = "NTEXT")
    private String notes;

    @Size(max = 250, message = "Đường dẫn hình ảnh không được vượt quá 250 ký tự")
    @Column(name = "ICON", length = 250)
    private String icon;

    @Column(name = "IDPARENT")
    private Long idParent;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 160, message = "Slug không được vượt quá 160 ký tự")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang")
    @Column(name = "SLUG", length = 160)
    private String slug;

    @Size(max = 100, message = "Meta Title không được vượt quá 100 ký tự")
    @Column(name = "META_TITLE", length = 100)
    private String metaTitle;

    @Size(max = 300, message = "Meta Keyword không được vượt quá 300 ký tự")
    @Column(name = "META_KEYWORD", length = 300)
    private String metaKeyword;

    @Size(max = 300, message = "Meta Description không được vượt quá 300 ký tự")
    @Column(name = "META_DESCRIPTION", length = 300)
    private String metaDescription;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    // Bỏ @NotNull vì giá trị sẽ được gán mặc định trong service
    @Min(value = 0, message = "Trạng thái xóa chỉ được là 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái xóa chỉ được là 0 hoặc 1")
    @Column(name = "ISDELETE")
    private Byte isDelete;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    @Min(value = 0, message = "Trạng thái hoạt động chỉ được là 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái hoạt động chỉ được là 0 hoặc 1")
    @Column(name = "ISACTIVE")
    private Byte isActive;
}