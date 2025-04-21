package com.devmaster.dev2503jwprj.repository;

import com.devmaster.dev2503jwprj.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIsDeleteFalse();

    Optional<Product> findByNameAndIsDeleteFalse(String name);

    Optional<Product> findBySlugAndIsDeleteFalse(String slug);

    @Query("SELECT p FROM Product p WHERE p.isDelete = 0 AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchByNameOrSlug(String keyword);
}