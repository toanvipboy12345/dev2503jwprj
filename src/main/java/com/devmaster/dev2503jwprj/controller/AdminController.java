package com.devmaster.dev2503jwprj.controller;

import com.devmaster.dev2503jwprj.dto.InventoryTransactionDTO;
import com.devmaster.dev2503jwprj.dto.ProductDTO;
import com.devmaster.dev2503jwprj.entity.Category;
import com.devmaster.dev2503jwprj.entity.Customer;
import com.devmaster.dev2503jwprj.repository.ConfigurationsRepository;
import com.devmaster.dev2503jwprj.service.CategoryService;
import com.devmaster.dev2503jwprj.service.CustomerService;
import com.devmaster.dev2503jwprj.service.ProductService;

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
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ConfigurationsRepository configurationsRepository;

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

        Set<Long> customerIds = categories.stream()
                .flatMap(category -> Stream.of(category.getCreatedBy(), category.getUpdatedBy()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> customerNames = customerIds.stream()
                .map(customerService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Customer::getId, Customer::getName, (name1, name2) -> name1));

        model.addAttribute("categories", categories);
        model.addAttribute("customerNames", customerNames);
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
    public String showEditCategoryForm(@PathVariable("id") Long id, Model model,
            RedirectAttributes redirectAttributes) {
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

    @PostMapping("/categories/delete/{id}")
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

    // @GetMapping("/products/list")
    // public String listProducts(Model model, @RequestParam(value = "keyword",
    // required = false) String keyword) {
    // List<ProductDTO> products;
    // if (keyword != null && !keyword.trim().isEmpty()) {
    // products = productService.searchProducts(keyword);
    // model.addAttribute("keyword", keyword);
    // } else {
    // products = productService.getAllProducts();
    // }

    // Set<Long> customerIds = products.stream()
    // .flatMap(product -> Stream.of(product.getCreatedBy(),
    // product.getUpdatedBy()))
    // .filter(Objects::nonNull)
    // .collect(Collectors.toSet());

    // Map<Long, String> customerNames = customerIds.stream()
    // .map(customerService::findById)
    // .filter(Optional::isPresent)
    // .map(Optional::get)
    // .collect(Collectors.toMap(Customer::getId, Customer::getName, (name1, name2)
    // -> name1));

    // model.addAttribute("products", products);
    // model.addAttribute("customerNames", customerNames);
    // model.addAttribute("categories", categoryService.getAllCategories());
    // model.addAttribute("configurations",
    // configurationsRepository.findByIsDeleteFalse());
    // model.addAttribute("breadcrumb", "Sản Phẩm");
    // return "admin/product/list";
    // }

    // @GetMapping("/products/customer-names")
    // @ResponseBody
    // public Map<Long, String> getCustomerNames() {
    // List<ProductDTO> products = productService.getAllProducts();
    // Set<Long> customerIds = products.stream()
    // .flatMap(product -> Stream.of(product.getCreatedBy(),
    // product.getUpdatedBy()))
    // .filter(Objects::nonNull)
    // .collect(Collectors.toSet());

    // return customerIds.stream()
    // .map(customerService::findById)
    // .filter(Optional::isPresent)
    // .map(Optional::get)
    // .collect(Collectors.toMap(Customer::getId, Customer::getName, (name1, name2)
    // -> name1));
    // }

    // @GetMapping("/products/add")
    // public String showAddProductForm(Model model) {
    // model.addAttribute("product", new ProductDTO());
    // model.addAttribute("categories", categoryService.getAllCategories());
    // model.addAttribute("configurations",
    // configurationsRepository.findByIsDeleteFalse());
    // model.addAttribute("breadcrumb", "Thêm Sản Phẩm");
    // return "admin/product/add";
    // }

    // @PostMapping("/products/add")
    // public String addProduct(@Valid @ModelAttribute("product") ProductDTO
    // productDTO, BindingResult result,
    // @RequestParam("imageFile") MultipartFile imageFile,
    // @RequestParam(value = "additionalImageFiles", required = false)
    // List<MultipartFile> additionalImageFiles,
    // Model model, RedirectAttributes redirectAttributes) {
    // logger.info("Nhận yêu cầu thêm sản phẩm: {}", productDTO);
    // logger.info("Configurations: {}", productDTO.getConfigurations());

    // if (result.hasErrors()) {
    // logger.info("Có lỗi validation khi thêm sản phẩm: {}",
    // result.getAllErrors());
    // model.addAttribute("categories", categoryService.getAllCategories());
    // model.addAttribute("configurations",
    // configurationsRepository.findByIsDeleteFalse());
    // model.addAttribute("breadcrumb", "Thêm Sản Phẩm");
    // return "admin/product/add";
    // }

    // try {
    // productService.createProduct(productDTO, imageFile, additionalImageFiles);
    // logger.info("Thêm sản phẩm thành công: {}", productDTO.getName());
    // redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành
    // công!");
    // return "redirect:/admin/products/list";
    // } catch (IllegalArgumentException e) {
    // logger.error("Lỗi khi thêm sản phẩm: {}", e.getMessage());
    // model.addAttribute("error", e.getMessage());
    // model.addAttribute("categories", categoryService.getAllCategories());
    // model.addAttribute("configurations",
    // configurationsRepository.findByIsDeleteFalse());
    // model.addAttribute("breadcrumb", "Thêm Sản Phẩm");
    // return "admin/product/add";
    // } catch (Exception e) {
    // logger.error("Có lỗi xảy ra khi thêm sản phẩm", e);
    // model.addAttribute("error", "Có lỗi xảy ra khi thêm sản phẩm");
    // model.addAttribute("categories", categoryService.getAllCategories());
    // model.addAttribute("configurations",
    // configurationsRepository.findByIsDeleteFalse());
    // model.addAttribute("breadcrumb", "Thêm Sản Phẩm");
    // return "admin/product/add";
    // }
    // }

    // @PostMapping("/products/edit/{id}")
    // public ResponseEntity<?> updateProduct(@PathVariable("id") Long id, @Valid
    // @ModelAttribute ProductDTO productDTO,
    // BindingResult result, @RequestParam("imageFile") MultipartFile imageFile,
    // @RequestParam(value = "additionalImageFiles", required = false)
    // List<MultipartFile> additionalImageFiles,
    // @RequestParam(value = "deleteImageIds", required = false) List<Long>
    // deleteImageIds) {
    // logger.info("Nhận yêu cầu sửa sản phẩm ID {}: {}", id, productDTO);

    // if (result.hasErrors()) {
    // Map<String, String> errors = new HashMap<>();
    // for (FieldError error : result.getFieldErrors()) {
    // errors.put(error.getField(), error.getDefaultMessage());
    // }
    // return ResponseEntity.badRequest().body(errors);
    // }

    // try {
    // productDTO.setId(id);
    // productService.updateProduct(productDTO, imageFile, additionalImageFiles,
    // deleteImageIds);
    // // Lấy dữ liệu mới nhất từ cơ sở dữ liệu sau khi cập nhật
    // ProductDTO updatedProduct = productService.getProductById(id)
    // .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại sau
    // khi cập nhật"));
    // logger.info("Sửa sản phẩm thành công: {}", updatedProduct.getName());
    // return ResponseEntity.ok(updatedProduct);
    // } catch (IllegalArgumentException e) {
    // logger.error("Lỗi khi sửa sản phẩm: {}", e.getMessage());
    // return ResponseEntity.badRequest().body(e.getMessage());
    // } catch (Exception e) {
    // logger.error("Có lỗi xảy ra khi sửa sản phẩm", e);
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi
    // xảy ra khi cập nhật sản phẩm");
    // }
    // }

    // @PostMapping("/products/delete/{id}")
    // public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes
    // redirectAttributes) {
    // logger.info("Nhận yêu cầu xóa sản phẩm ID: {}", id);

    // try {
    // productService.deleteProduct(id);
    // logger.info("Xóa sản phẩm thành công: ID {}", id);
    // redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành
    // công!");
    // return "redirect:/admin/products/list";
    // } catch (IllegalArgumentException e) {
    // logger.error("Lỗi khi xóa sản phẩm: {}", e.getMessage());
    // redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    // return "redirect:/admin/products/list";
    // }
    // }
    @GetMapping("/products/list")
    public String listProducts(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<ProductDTO> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.searchProducts(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            products = productService.getAllProducts();
        }

        Set<Long> customerIds = products.stream()
                .flatMap(product -> Stream.of(product.getCreatedBy(), product.getUpdatedBy()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> customerNames = customerIds.stream()
                .map(customerService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Customer::getId, Customer::getName, (name1, name2) -> name1));

        model.addAttribute("products", products);
        model.addAttribute("customerNames", customerNames);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("configurations", configurationsRepository.findByIsDeleteFalse());
        model.addAttribute("breadcrumb", "Sản Phẩm");
        return "admin/product/list"; // Đúng đường dẫn
    }

    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("configurations", configurationsRepository.findByIsDeleteFalse());
        model.addAttribute("breadcrumb", "Thêm Sản Phẩm");
        return "admin/product/add"; // Đúng đường dẫn
    }

    @PostMapping("/products/add")
    public String addProduct(@Valid @ModelAttribute("product") ProductDTO productDTO, BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "additionalImageFiles", required = false) List<MultipartFile> additionalImageFiles,
            Model model, RedirectAttributes redirectAttributes) {
        logger.info("Nhận yêu cầu thêm sản phẩm: {}", productDTO);
        logger.info("Configurations: {}", productDTO.getConfigurations());

        if (result.hasErrors()) {
            logger.info("Có lỗi validation khi thêm sản phẩm: {}", result.getAllErrors());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("configurations", configurationsRepository.findByIsDeleteFalse());
            model.addAttribute("breadcrumb", "Thêm Sản Phẩm");
            return "admin/product/add"; // Đúng đường dẫn
        }

        try {
            productService.createProduct(productDTO, imageFile, additionalImageFiles);
            logger.info("Thêm sản phẩm thành công: {}", productDTO.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
            return "redirect:/admin/products/list";
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi thêm sản phẩm: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("configurations", configurationsRepository.findByIsDeleteFalse());
            model.addAttribute("breadcrumb", "Thêm Sản Phẩm");
            return "admin/product/add"; // Đúng đường dẫn
        } catch (Exception e) {
            logger.error("Có lỗi xảy ra khi thêm sản phẩm", e);
            model.addAttribute("error", "Có lỗi xảy ra khi thêm sản phẩm");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("configurations", configurationsRepository.findByIsDeleteFalse());
            model.addAttribute("breadcrumb", "Thêm Sản Phẩm");
            return "admin/product/add"; // Đúng đường dẫn
        }
    }

    @GetMapping("/inventory/list")
    public String listInventory(Model model) {
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("breadcrumb", "Quản Lý Kho");
        return "admin/inventory/list";
    }

    @GetMapping("/inventory/stock-in")
    public String showStockInForm(@RequestParam("productId") Long productId, Model model) {
        ProductDTO product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));
        model.addAttribute("product", product);
        model.addAttribute("transaction", new InventoryTransactionDTO());
        model.addAttribute("breadcrumb", "Nhập Kho");
        return "admin/inventory/stock-in";
    }

    @PostMapping("/inventory/stock-in")
    public String stockIn(@RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes) {
        try {
            productService.stockIn(productId, quantity, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Nhập kho thành công!");
            return "redirect:/admin/inventory/list";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/inventory/stock-in?productId=" + productId;
        }
    }

    @GetMapping("/inventory/adjust")
    public String showAdjustForm(@RequestParam("productId") Long productId, Model model) {
        ProductDTO product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));
        model.addAttribute("product", product);
        model.addAttribute("transaction", new InventoryTransactionDTO());
        model.addAttribute("breadcrumb", "Điều Chỉnh Kho");
        return "admin/inventory/adjust";
    }

    @PostMapping("/inventory/adjust")
    public String adjustStock(@RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes) {
        try {
            productService.adjustStock(productId, quantity, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Điều chỉnh kho thành công!");
            return "redirect:/admin/inventory/list";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/inventory/adjust?productId=" + productId;
        }
    }
}