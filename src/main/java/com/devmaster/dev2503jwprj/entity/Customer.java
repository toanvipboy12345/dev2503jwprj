package com.devmaster.dev2503jwprj.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CUSTOMER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", length = 250)
    private String name;

    @Column(name = "USERNAME", length = 50, unique = true)
    private String username;

    @Column(name = "PASSWORD", length = 255) // Tăng kích thước để lưu mật khẩu mã hóa
    private String password;

    @Column(name = "ADDRESS", length = 500)
    private String address; // Thêm cột ADDRESS

    @Column(name = "EMAIL", length = 150, unique = true)
    private String email;

    @Column(name = "PHONE", length = 50)
    private String phone;

    @Column(name = "AVATAR", length = 250)
    private String avatar;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "ISDELETE")
    private Byte isDelete;

    @Column(name = "ISACTIVE")
    private Byte isActive;

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.username);
    }
}