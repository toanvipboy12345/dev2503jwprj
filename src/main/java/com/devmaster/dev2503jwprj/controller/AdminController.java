package com.devmaster.dev2503jwprj.controller;

import com.devmaster.dev2503jwprj.entity.Category;
import com.devmaster.dev2503jwprj.entity.Customer;
import com.devmaster.dev2503jwprj.service.CategoryService;
import com.devmaster.dev2503jwprj.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CustomerService customerService; // Thêm CustomerService

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("breadcrumb", "Dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/categories/list")
    public String listCategories(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<Category> categories;
        if (keyword != null && !keyword.trim().isEmpty()) {
            categories = categoryService.searchCategories(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            categories = categoryService.getAllCategories();
        }

        // Lấy danh sách ID của createdBy và updatedBy
        Set<Long> customerIds = categories.stream()
                .flatMap(category -> Stream.of(category.getCreatedBy(), category.getUpdatedBy()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Lấy danh sách Customer tương ứng
        Map<Long, String> customerNames = customerIds.stream()
                .map(customerService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Customer::getId, Customer::getName, (name1, name2) -> name1));

        model.addAttribute("categories", categories);
        model.addAttribute("customerNames", customerNames); // Truyền customerNames vào model
        model.addAttribute("parentCategories", categoryService.getParentCategories());
        model.addAttribute("breadcrumb", "Danh Mục");
        return "admin/category/list";
    }

    @GetMapping("/categories/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("parentCategories", categoryService.getParentCategories());
        model.addAttribute("breadcrumb", "Thêm Danh Mục");
        return "admin/category/add";
    }

    @PostMapping("/categories/add")
    public String addCategory(@Valid @ModelAttribute("category") Category category, BindingResult result,
                              @RequestParam("iconFile") MultipartFile iconFile, Model model,
                              RedirectAttributes redirectAttributes) {
        logger.info("Nhận yêu cầu thêm danh mục: {}", category);

        if (result.hasErrors()) {
            logger.info("Có lỗi validation khi thêm danh mục: {}", result.getAllErrors());
            model.addAttribute("parentCategories", categoryService.getParentCategories());
            model.addAttribute("breadcrumb", "Thêm Danh Mục");
            return "admin/category/add";
        }

        try {
            categoryService.createCategory(category, iconFile);
            logger.info("Thêm danh mục thành công: {}", category.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
            return "redirect:/admin/categories/list";
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi thêm danh mục: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("parentCategories", categoryService.getParentCategories());
            model.addAttribute("breadcrumb", "Thêm Danh Mục");
            return "admin/category/add";
        } catch (Exception e) {
            logger.error("Có lỗi xảy ra khi thêm danh mục", e);
            model.addAttribute("error", "Có lỗi xảy ra khi thêm danh mục");
            model.addAttribute("parentCategories", categoryService.getParentCategories());
            model.addAttribute("breadcrumb", "Thêm Danh Mục");
            return "admin/category/add";
        }
    }

    @GetMapping("/categories/edit/{id}")
    public String showEditCategoryForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại: " + id));
            model.addAttribute("category", category);
            model.addAttribute("parentCategories", categoryService.getParentCategories());
            model.addAttribute("breadcrumb", "Sửa Danh Mục");
            return "admin/category/edit";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/categories/list";
        }
    }

    @PostMapping("/categories/edit/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable("id") Long id, @Valid @ModelAttribute Category category,
                                           BindingResult result, @RequestParam("iconFile") MultipartFile iconFile) {
        logger.info("Nhận yêu cầu sửa danh mục ID {}: {}", id, category);
    
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }
    
        try {
            category.setId(id);
            categoryService.updateCategory(category, iconFile);
            logger.info("Sửa danh mục thành công: {}, icon: {}", category.getName(), category.getIcon());
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi sửa danh mục: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Có lỗi xảy ra khi sửa danh mục", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra khi cập nhật danh mục");
        }
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Nhận yêu cầu xóa danh mục ID: {}", id);

        try {
            categoryService.deleteCategory(id);
            logger.info("Xóa danh mục thành công: ID {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
            return "redirect:/admin/categories/list";
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi xóa danh mục: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/categories/list";
        }
    }
}