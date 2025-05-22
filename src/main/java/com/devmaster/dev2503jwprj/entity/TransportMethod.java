package com.devmaster.dev2503jwprj.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "transport_method")
@Data
public class TransportMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Tên phương thức vận chuyển không được để trống")
    @Size(max = 250, message = "Tên phương thức vận chuyển không được vượt quá 250 ký tự")
    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Min(value = 0, message = "Trạng thái xóa chỉ được là 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái xóa chỉ được là 0 hoặc 1")
    @Column(name = "isdelete")
    private Byte isDelete;

    @NotNull(message = "Trạng thái sử dụng không được để trống")
    @Min(value = 0, message = "Trạng thái sử dụng chỉ được là 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái sử dụng chỉ được là 0 hoặc 1")
    @Column(name = "isactive", nullable = false)
    private Byte isActive;
}