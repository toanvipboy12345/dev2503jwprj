package com.devmaster.dev2503jwprj.repository;

import com.devmaster.dev2503jwprj.entity.ProductImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImagesRepository extends JpaRepository<ProductImages, Long> {
    List<ProductImages> findByIdProduct(Long idProduct);
    void deleteByIdProduct(Long idProduct);
}