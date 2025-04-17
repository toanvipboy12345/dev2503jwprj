package com.devmaster.dev2503jwprj.repository;

import com.devmaster.dev2503jwprj.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Tìm kiếm danh mục theo tên hoặc slug, không phân biệt hoa thường
    @Query("SELECT c FROM Category c WHERE (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND c.isDelete = 0")
    List<Category> searchByNameOrSlug(@Param("keyword") String keyword);

    // Lấy danh sách danh mục cha (IDPARENT = NULL)
    List<Category> findByIdParentIsNullAndIsDeleteFalse();

    // Lấy danh sách danh mục không bị xóa
    List<Category> findByIsDeleteFalse();

    // Tìm danh mục theo tên (không tính danh mục đã xóa)
    Optional<Category> findByNameAndIsDeleteFalse(String name);

    // Tìm danh mục theo slug (không tính danh mục đã xóa)
    Optional<Category> findBySlugAndIsDeleteFalse(String slug);
}