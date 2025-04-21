package com.devmaster.dev2503jwprj.controller;

import com.devmaster.dev2503jwprj.dto.CartItemDTO;
import com.devmaster.dev2503jwprj.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Hiển thị giỏ hàng
@GetMapping
public String viewCart(HttpSession session, Authentication authentication, Model model) {
    List<CartItemDTO> cartItems;
    if (authentication != null && authentication.isAuthenticated()) {
        cartItems = cartService.getCartFromDatabase(authentication.getName());
    } else {
        cartItems = cartService.getCart(session);
    }
    // Tính tổng giá trị giỏ hàng
    BigDecimal totalPrice = cartItems.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    model.addAttribute("cartItems", cartItems);
    model.addAttribute("totalPrice", totalPrice); // Thêm tổng giá vào model
    return "cart/cart";
}

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, @RequestParam Integer quantity,
                            HttpSession session, Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        if (cartService.addToCart(session, authentication, productId, quantity)) {
            redirectAttributes.addFlashAttribute("message", "Thêm sản phẩm vào giỏ hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể thêm sản phẩm vào giỏ hàng. Vui lòng kiểm tra số lượng hoặc sản phẩm.");
        }
        return "redirect:/cart";
    }

    // Cập nhật số lượng
    @PostMapping("/update")
    public String updateCart(@RequestParam Long productId, @RequestParam Integer quantity,
                             HttpSession session, Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (cartService.updateCartItem(session, authentication, productId, quantity)) {
            redirectAttributes.addFlashAttribute("message", "Cập nhật giỏ hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật số lượng. Vui lòng kiểm tra lại.");
        }
        return "redirect:/cart";
    }

    // Xóa sản phẩm
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId, HttpSession session,
                                 Authentication authentication, RedirectAttributes redirectAttributes) {
        cartService.removeCartItem(session, authentication, productId);
        redirectAttributes.addFlashAttribute("message", "Xóa sản phẩm khỏi giỏ hàng thành công!");
        return "redirect:/cart";
    }
}