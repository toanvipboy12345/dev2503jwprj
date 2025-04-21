package com.devmaster.dev2503jwprj.repository;

import com.devmaster.dev2503jwprj.entity.ProductConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductConfigRepository extends JpaRepository<ProductConfig, Long> {
    List<ProductConfig> findByIdProduct(Long idProduct);
    void deleteByIdProduct(Long idProduct);
}