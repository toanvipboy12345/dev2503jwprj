package com.devmaster.dev2503jwprj.service;

import com.devmaster.dev2503jwprj.entity.Category;
import com.devmaster.dev2503jwprj.entity.Customer;
import com.devmaster.dev2503jwprj.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Validated
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerService customerService; // Thêm CustomerService để lấy thông tin người dùng

    private static final String UPLOAD_DIR = "src/main/resources/static/images/categories/";
    private static final String IMAGE_URL_PREFIX = "/images/categories/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_FILE_TYPES = {"image/jpeg", "image/png", "image/gif"};

    @PostConstruct
    public void init() throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findByIsDeleteFalse();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .filter(category -> category.getIsDelete() == null || category.getIsDelete() == 0);
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        Customer customer = customerService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
        return customer.getId();
    }

    public void createCategory(@Valid Category category, MultipartFile iconFile) throws IOException {
        if (categoryRepository.findByNameAndIsDeleteFalse(category.getName()).isPresent()) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }

        if (categoryRepository.findBySlugAndIsDeleteFalse(category.getSlug()).isPresent()) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }

        if (iconFile != null && !iconFile.isEmpty()) {
            if (iconFile.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
            }

            String contentType = iconFile.getContentType();
            boolean isValidType = false;
            for (String type : ALLOWED_FILE_TYPES) {
                if (type.equalsIgnoreCase(contentType)) {
                    isValidType = true;
                    break;
                }
            }
            if (!isValidType) {
                throw new IllegalArgumentException("Chỉ hỗ trợ các định dạng ảnh: JPEG, PNG, GIF");
            }

            String fileName = UUID.randomUUID() + "_" + iconFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, iconFile.getBytes());
            category.setIcon(IMAGE_URL_PREFIX + fileName);
        }

        category.setCreatedDate(LocalDateTime.now());
        category.setCreatedBy(getCurrentUserId()); // Lưu ID người tạo
        category.setIsDelete((byte) 0);
        category.setIsActive((byte) 1);
        categoryRepository.save(category);
    }

    public void updateCategory(@Valid Category category, MultipartFile iconFile) throws IOException {
        Optional<Category> existingCategoryOpt = categoryRepository.findById(category.getId());
        if (!existingCategoryOpt.isPresent()) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }
    
        Category existingCategory = existingCategoryOpt.get();
    
        Optional<Category> categoryWithSameName = categoryRepository.findByNameAndIsDeleteFalse(category.getName());
        if (categoryWithSameName.isPresent() && !categoryWithSameName.get().getId().equals(category.getId())) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }
    
        Optional<Category> categoryWithSameSlug = categoryRepository.findBySlugAndIsDeleteFalse(category.getSlug());
        if (categoryWithSameSlug.isPresent() && !categoryWithSameSlug.get().getId().equals(category.getId())) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }
    
        // Xử lý file ảnh
        if (iconFile != null && !iconFile.isEmpty()) {
            if (iconFile.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
            }
    
            String contentType = iconFile.getContentType();
            boolean isValidType = false;
            for (String type : ALLOWED_FILE_TYPES) {
                if (type.equalsIgnoreCase(contentType)) {
                    isValidType = true;
                    break;
                }
            }
            if (!isValidType) {
                throw new IllegalArgumentException("Chỉ hỗ trợ các định dạng ảnh: JPEG, PNG, GIF");
            }
    
            String fileName = UUID.randomUUID() + "_" + iconFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, iconFile.getBytes());
            category.setIcon(IMAGE_URL_PREFIX + fileName);
        } else {
            // Giữ nguyên giá trị icon hiện tại nếu không có file ảnh mới
            category.setIcon(existingCategory.getIcon());
        }
    
        // Giữ nguyên createdBy và createdDate từ danh mục hiện tại
        category.setCreatedBy(existingCategory.getCreatedBy());
        category.setCreatedDate(existingCategory.getCreatedDate());
    
        // Cập nhật các trường khác
        category.setIsDelete(existingCategory.getIsDelete());
        category.setUpdatedDate(LocalDateTime.now());
        category.setUpdatedBy(getCurrentUserId()); // Lưu ID người sửa
        categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (!categoryOpt.isPresent()) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }
        Category category = categoryOpt.get();
        if (category.getIsDelete() == 1) {
            throw new IllegalArgumentException("Danh mục đã bị xóa trước đó");
        }
        category.setIsDelete((byte) 1);
        category.setUpdatedDate(LocalDateTime.now());
        category.setUpdatedBy(getCurrentUserId()); // Lưu ID người xóa
        categoryRepository.save(category);
    }

    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return categoryRepository.findByIsDeleteFalse();
        }
        return categoryRepository.searchByNameOrSlug(keyword);
    }

    public List<Category> getParentCategories() {
        return categoryRepository.findByIdParentIsNullAndIsDeleteFalse();
    }
}