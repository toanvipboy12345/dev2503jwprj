package com.devmaster.dev2503jwprj.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PRODUCT_CONFIG")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "IDPRODUCT")
    private Long idProduct;

    @Column(name = "IDCONFIG")
    private Long idConfig;

    @Column(name = "VALUE", columnDefinition = "TEXT")
    private String value;
}