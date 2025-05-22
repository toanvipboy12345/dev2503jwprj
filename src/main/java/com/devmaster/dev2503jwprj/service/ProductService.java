package com.devmaster.dev2503jwprj.service;

import com.devmaster.dev2503jwprj.dto.ProductDTO;
import com.devmaster.dev2503jwprj.entity.Category;
import com.devmaster.dev2503jwprj.entity.Configurations;
import com.devmaster.dev2503jwprj.entity.Product;
import com.devmaster.dev2503jwprj.entity.ProductConfig;
import com.devmaster.dev2503jwprj.entity.ProductImages;
import com.devmaster.dev2503jwprj.repository.CategoryRepository;
import com.devmaster.dev2503jwprj.repository.ConfigurationsRepository;
import com.devmaster.dev2503jwprj.repository.ProductConfigRepository;
import com.devmaster.dev2503jwprj.repository.ProductImagesRepository;
import com.devmaster.dev2503jwprj.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import com.devmaster.dev2503jwprj.entity.InventoryTransaction;
import com.devmaster.dev2503jwprj.repository.InventoryTransactionRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ConfigurationsRepository configurationsRepository;

    @Autowired
    private ProductConfigRepository productConfigRepository;

    @Autowired
    private ProductImagesRepository productImagesRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/images/products/";
    private static final String IMAGE_URL_PREFIX = "/images/products/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_FILE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    @PostConstruct
    public void init() throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

public List<ProductDTO> getAllProducts() {
    List<Product> products = productRepository.findByIsDeleteFalse();
    System.out.println("Products found: " + products.size());
    products.forEach(p -> System.out.println("Product ID: " + p.getId() + ", Name: " + p.getName() + ", Quantity: " + p.getQuantity()));
    List<ProductDTO> productDTOs = new ArrayList<>();
    for (Product p : products) {
        try {
            productDTOs.add(toDTO(p));
        } catch (Exception e) {
            System.err.println("Error mapping product ID " + p.getId() + ": " + e.getMessage());
        }
    }
    System.out.println("ProductDTOs created: " + productDTOs.size());
    return productDTOs;
}

    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .filter(product -> product.getIsDelete() == 0)
                .map(this::toDTO);
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return customerService.findByUsername(username)
                .map(customer -> customer.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }

    @Transactional
    public void createProduct(@Valid ProductDTO productDTO, MultipartFile imageFile, List<MultipartFile> additionalImageFiles) throws IOException {
        if (productRepository.findByNameAndIsDeleteFalse(productDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Tên sản phẩm đã tồn tại");
        }

        if (productRepository.findBySlugAndIsDeleteFalse(productDTO.getSlug()).isPresent()) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }

        Product product = toEntity(productDTO);

        if (imageFile != null && !imageFile.isEmpty()) {
            validateImageFile(imageFile);
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, imageFile.getBytes());
            product.setImage(IMAGE_URL_PREFIX + fileName);
        }

        product.setCreatedDate(LocalDateTime.now());
        product.setCreatedBy(getCurrentUserId());
        product.setIsDelete((byte) 0);
        product.setIsActive((byte) 1);
        product = productRepository.save(product);

        if (productDTO.getQuantity() != null && productDTO.getQuantity() > 0) {
            InventoryTransaction transaction = InventoryTransaction.builder()
                    .productId(product.getId())
                    .transactionType("STOCK_IN")
                    .quantity(productDTO.getQuantity())
                    .reason("Nhập kho khi tạo sản phẩm")
                    .createdDate(LocalDateTime.now())
                    .createdBy(getCurrentUserId())
                    .build();
            inventoryTransactionRepository.save(transaction);
        }

        saveConfigurations(product.getId(), productDTO.getConfigurations());
        saveAdditionalImages(product.getId(), additionalImageFiles);
    }

    @Transactional
    public void updateProduct(@Valid ProductDTO productDTO, MultipartFile imageFile, List<MultipartFile> additionalImageFiles, List<Long> deleteImageIds) throws IOException {
        Optional<Product> existingProductOpt = productRepository.findById(productDTO.getId());
        if (!existingProductOpt.isPresent() || existingProductOpt.get().getIsDelete() == 1) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xóa");
        }

        Product existingProduct = existingProductOpt.get();

        Optional<Product> productWithSameName = productRepository.findByNameAndIsDeleteFalse(productDTO.getName());
        if (productWithSameName.isPresent() && !productWithSameName.get().getId().equals(productDTO.getId())) {
            throw new IllegalArgumentException("Tên sản phẩm đã tồn tại");
        }

        Optional<Product> productWithSameSlug = productRepository.findBySlugAndIsDeleteFalse(productDTO.getSlug());
        if (productWithSameSlug.isPresent() && !productWithSameSlug.get().getId().equals(productDTO.getId())) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }

        Product product = toEntity(productDTO);

        if (imageFile != null && !imageFile.isEmpty()) {
            validateImageFile(imageFile);
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, imageFile.getBytes());
            product.setImage(IMAGE_URL_PREFIX + fileName);
        } else {
            product.setImage(existingProduct.getImage());
        }

        product.setCreatedBy(existingProduct.getCreatedBy());
        product.setCreatedDate(existingProduct.getCreatedDate());
        product.setIsDelete(existingProduct.getIsDelete());
        product.setUpdatedDate(LocalDateTime.now());
        product.setUpdatedBy(getCurrentUserId());
        product = productRepository.save(product);

        productConfigRepository.deleteByIdProduct(product.getId());
        saveConfigurations(product.getId(), productDTO.getConfigurations());

        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            deleteImagesByIds(deleteImageIds);
        }

        saveAdditionalImages(product.getId(), additionalImageFiles);
    }

    private void deleteImagesByIds(List<Long> imageIds) {
        for (Long imageId : imageIds) {
            productImagesRepository.findById(imageId).ifPresent(image -> {
                try {
                    Path filePath = Paths.get(UPLOAD_DIR, image.getUrlImg().replace(IMAGE_URL_PREFIX, ""));
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.err.println("Không thể xóa file ảnh: " + e.getMessage());
                }
                productImagesRepository.deleteById(imageId);
            });
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (!productOpt.isPresent() || productOpt.get().getIsDelete() == 1) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xóa");
        }
        Product product = productOpt.get();
        product.setIsDelete((byte) 1);
        product.setUpdatedDate(LocalDateTime.now());
        product.setUpdatedBy(getCurrentUserId());
        productRepository.save(product);
    }

    public List<ProductDTO> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchByNameOrSlug(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void validateImageFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }
        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String type : ALLOWED_FILE_TYPES) {
            if (type.equalsIgnoreCase(contentType)) {
                isValidType = true;
                break;
            }
        }
        if (!isValidType) {
            throw new IllegalArgumentException("Chỉ hỗ trợ các định dạng ảnh: JPEG, PNG, GIF, WebP");
        }
    }

    private void saveConfigurations(Long productId, List<ProductDTO.ProductConfigDTO> configDTOs) {
        if (configDTOs != null) {
            for (ProductDTO.ProductConfigDTO configDTO : configDTOs) {
                if (configDTO.getConfigName() != null && !configDTO.getConfigName().trim().isEmpty() &&
                    configDTO.getValue() != null && !configDTO.getValue().trim().isEmpty()) {
                    Configurations configEntity = configurationsRepository.findByNameAndIsDeleteFalse(configDTO.getConfigName())
                            .orElseGet(() -> {
                                Configurations newConfig = Configurations.builder()
                                        .name(configDTO.getConfigName())
                                        .notes(configDTO.getNotes() != null ? configDTO.getNotes() : "")
                                        .isDelete((byte) 0)
                                        .isActive((byte) 1)
                                        .build();
                                return configurationsRepository.save(newConfig);
                            });
                    ProductConfig config = ProductConfig.builder()
                            .idProduct(productId)
                            .idConfig(configEntity.getId())
                            .value(configDTO.getValue())
                            .build();
                    productConfigRepository.save(config);
                }
            }
        }
    }

    private void saveAdditionalImages(Long productId, List<MultipartFile> imageFiles) throws IOException {
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    validateImageFile(file);
                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(UPLOAD_DIR, fileName);
                    Files.write(filePath, file.getBytes());
                    ProductImages image = ProductImages.builder()
                            .name(file.getOriginalFilename())
                            .urlImg(IMAGE_URL_PREFIX + fileName)
                            .idProduct(productId)
                            .build();
                    productImagesRepository.save(image);
                }
            }
        }
    }

private ProductDTO toDTO(Product product) {
    List<ProductDTO.ProductConfigDTO> configDTOs = productConfigRepository.findByIdProduct(product.getId()).stream()
            .map(config -> {
                Optional<Configurations> configEntity = configurationsRepository.findById(config.getIdConfig());
                return ProductDTO.ProductConfigDTO.builder()
                        .id(config.getId())
                        .idConfig(config.getIdConfig())
                        .configName(configEntity.map(Configurations::getName).orElse("Unknown"))
                        .value(config.getValue())
                        .build();
            })
            .collect(Collectors.toList());

    List<ProductDTO.ProductImageDTO> imageDTOs = productImagesRepository.findByIdProduct(product.getId()).stream()
            .map(image -> ProductDTO.ProductImageDTO.builder()
                    .id(image.getId())
                    .name(image.getName())
                    .urlImg(image.getUrlImg())
                    .build())
            .collect(Collectors.toList());

    String categoryName = product.getIdCategory() != null
            ? categoryRepository.findById(product.getIdCategory())
                    .map(Category::getName)
                    .orElse("Không có")
            : "Không có";

    return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName() != null ? product.getName() : "Không xác định")
            .description(product.getDescription())
            .notes(product.getNotes())
            .image(product.getImage())
            .idCategory(product.getIdCategory())
            .categoryName(categoryName)
            .contents(product.getContents())
            .price(product.getPrice())
            .quantity(product.getQuantity() != null ? product.getQuantity() : 0)
            .slug(product.getSlug())
            .metaTitle(product.getMetaTitle())
            .metaKeyword(product.getMetaKeyword())
            .metaDescription(product.getMetaDescription())
            .createdDate(product.getCreatedDate())
            .updatedDate(product.getUpdatedDate())
            .createdBy(product.getCreatedBy())
            .updatedBy(product.getUpdatedBy())
            .isDelete(product.getIsDelete())
            .isActive(product.getIsActive())
            .configurations(configDTOs)
            .images(imageDTOs)
            .build();
}

    private Product toEntity(ProductDTO dto) {
        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .notes(dto.getNotes())
                .image(dto.getImage())
                .idCategory(dto.getIdCategory())
                .contents(dto.getContents())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .slug(dto.getSlug())
                .metaTitle(dto.getMetaTitle())
                .metaKeyword(dto.getMetaKeyword())
                .metaDescription(dto.getMetaDescription())
                .createdDate(dto.getCreatedDate())
                .updatedDate(dto.getUpdatedDate())
                .createdBy(dto.getCreatedBy())
                .updatedBy(dto.getUpdatedBy())
                .isDelete(dto.getIsDelete())
                .isActive(dto.getIsActive())
                .build();
    }

    @Transactional
    public void stockIn(Long productId, Integer quantity, String reason) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập kho phải lớn hơn 0");
        }
        Product product = productRepository.findById(productId)
                .filter(p -> p.getIsDelete() == 0)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xóa"));
        product.setQuantity(product.getQuantity() + quantity);
        product.setUpdatedDate(LocalDateTime.now());
        product.setUpdatedBy(getCurrentUserId());
        productRepository.save(product);

        InventoryTransaction transaction = InventoryTransaction.builder()
                .productId(productId)
                .transactionType("STOCK_IN")
                .quantity(quantity)
                .reason(reason)
                .createdDate(LocalDateTime.now())
                .createdBy(getCurrentUserId())
                .build();
        inventoryTransactionRepository.save(transaction);
    }

    @Transactional
    public void stockOut(Long productId, Integer quantity, String reason) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng xuất kho phải lớn hơn 0");
        }
        Product product = productRepository.findById(productId)
                .filter(p -> p.getIsDelete() == 0)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xóa"));
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Không đủ hàng trong kho");
        }
        product.setQuantity(product.getQuantity() - quantity);
        product.setUpdatedDate(LocalDateTime.now());
        product.setUpdatedBy(getCurrentUserId());
        productRepository.save(product);

        InventoryTransaction transaction = InventoryTransaction.builder()
                .productId(productId)
                .transactionType("STOCK_OUT")
                .quantity(-quantity)
                .reason(reason)
                .createdDate(LocalDateTime.now())
                .createdBy(getCurrentUserId())
                .build();
        inventoryTransactionRepository.save(transaction);
    }

    @Transactional
    public void adjustStock(Long productId, Integer quantity, String reason) {
        Product product = productRepository.findById(productId)
                .filter(p -> p.getIsDelete() == 0)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xóa"));
        int newQuantity = product.getQuantity() + quantity;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Số lượng kho không thể âm");
        }
        product.setQuantity(newQuantity);
        product.setUpdatedDate(LocalDateTime.now());
        product.setUpdatedBy(getCurrentUserId());
        productRepository.save(product);

        InventoryTransaction transaction = InventoryTransaction.builder()
                .productId(productId)
                .transactionType("ADJUSTMENT")
                .quantity(quantity)
                .reason(reason)
                .createdDate(LocalDateTime.now())
                .createdBy(getCurrentUserId())
                .build();
        inventoryTransactionRepository.save(transaction);
    }
}