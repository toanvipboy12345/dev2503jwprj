package com.devmaster.dev2503jwprj.controller;

import com.devmaster.dev2503jwprj.dto.ProductDTO;
import com.devmaster.dev2503jwprj.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public String listProducts(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        logger.info("Truy cập endpoint /products với keyword: {}", keyword);
        List<ProductDTO> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.searchProducts(keyword).stream()
                    .filter(product -> product.getIsActive() == 1)
                    .collect(Collectors.toList());
            model.addAttribute("keyword", keyword);
        } else {
            products = productService.getAllProducts().stream()
                    .filter(product -> product.getIsActive() == 1)
                    .collect(Collectors.toList());
        }
        model.addAttribute("products", products);
        model.addAttribute("breadcrumb", "Sản Phẩm");
        logger.info("Trả về template home/list với số lượng sản phẩm: {}", products.size());
        return "home/list";
    }

    @GetMapping("/products/{slug}")
    public String productDetail(@PathVariable("slug") String slug, Model model) {
        logger.info("Truy cập endpoint /products/{} để xem chi tiết sản phẩm", slug);
        Optional<ProductDTO> productOpt = productService.getAllProducts().stream()
                .filter(product -> product.getSlug().equals(slug) && product.getIsActive() == 1)
                .findFirst();

        if (!productOpt.isPresent()) {
            logger.warn("Không tìm thấy sản phẩm với slug: {}, chuyển hướng về /products", slug);
            return "redirect:/products";
        }

        model.addAttribute("product", productOpt.get());
        model.addAttribute("breadcrumb", productOpt.get().getName());
        logger.info("Trả về template home/detail cho sản phẩm: {}", productOpt.get().getName());
        return "home/detail";
    }
}