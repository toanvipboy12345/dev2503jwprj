package com.devmaster.dev2503jwprj.service;

import com.devmaster.dev2503jwprj.dto.CartItemDTO;
import com.devmaster.dev2503jwprj.entity.Cart;
import com.devmaster.dev2503jwprj.entity.CartItem;
import com.devmaster.dev2503jwprj.entity.Customer;
import com.devmaster.dev2503jwprj.entity.Product;
import com.devmaster.dev2503jwprj.repository.CartItemRepository;
import com.devmaster.dev2503jwprj.repository.CartRepository;
import com.devmaster.dev2503jwprj.repository.CustomerRepository;
import com.devmaster.dev2503jwprj.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "cart";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // Lấy giỏ hàng từ session
    @SuppressWarnings("unchecked")
    public List<CartItemDTO> getCart(HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    // Lấy giỏ hàng từ cơ sở dữ liệu
    public List<CartItemDTO> getCartFromDatabase(String username) {
        List<CartItemDTO> cartItems = new ArrayList<>();
        Optional<Customer> customerOpt = customerRepository.findByUsername(username);
        if (customerOpt.isPresent()) {
            Optional<Cart> cartOpt = cartRepository.findByCustomerId(customerOpt.get().getId());
            if (cartOpt.isPresent()) {
                List<CartItem> items = cartItemRepository.findByCartId(cartOpt.get().getId());
                for (CartItem item : items) {
                    cartItems.add(CartItemDTO.builder()
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .price(item.getProduct().getPrice())
                            .image(item.getProduct().getImage())
                            .quantity(item.getQuantity())
                            .build());
                }
            }
        }
        return cartItems;
    }

    // Đồng bộ giỏ hàng từ session vào cơ sở dữ liệu
    public void syncCartFromSession(HttpSession session, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        String username = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByUsername(username);
        if (customerOpt.isEmpty()) {
            return;
        }
        Customer customer = customerOpt.get();
        List<CartItemDTO> sessionCart = getCart(session);
        if (sessionCart.isEmpty()) {
            return;
        }

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .customer(customer)
                            .createdDate(LocalDateTime.now())
                            .build();
                    return cartRepository.save(newCart);
                });

        for (CartItemDTO itemDTO : sessionCart) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .filter(p -> p.getIsDelete() == 0)
                    .orElse(null);
            if (product != null && product.getQuantity() >= itemDTO.getQuantity()) {
                Optional<CartItem> existingItem = cartItemRepository.findByCartId(cart.getId())
                        .stream()
                        .filter(ci -> ci.getProduct().getId().equals(itemDTO.getProductId()))
                        .findFirst();
                if (existingItem.isPresent()) {
                    CartItem cartItem = existingItem.get();
                    cartItem.setQuantity(cartItem.getQuantity() + itemDTO.getQuantity());
                    cartItemRepository.save(cartItem);
                } else {
                    CartItem cartItem = CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(itemDTO.getQuantity())
                            .build();
                    cartItemRepository.save(cartItem);
                }
            }
        }
        clearCart(session, authentication);
    }

    // Thêm sản phẩm vào giỏ hàng
    public boolean addToCart(HttpSession session, Authentication authentication, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .filter(p -> p.getIsDelete() == 0)
                .orElse(null);
        if (product == null || product.getQuantity() < quantity || quantity <= 0) {
            return false;
        }

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Customer> customerOpt = customerRepository.findByUsername(username);
            if (customerOpt.isEmpty()) {
                return false;
            }
            Customer customer = customerOpt.get();
            Cart cart = cartRepository.findByCustomerId(customer.getId())
                    .orElseGet(() -> {
                        Cart newCart = Cart.builder()
                                .customer(customer)
                                .createdDate(LocalDateTime.now())
                                .build();
                        return cartRepository.save(newCart);
                    });

            Optional<CartItem> existingItem = cartItemRepository.findByCartId(cart.getId())
                    .stream()
                    .filter(ci -> ci.getProduct().getId().equals(productId))
                    .findFirst();
            if (existingItem.isPresent()) {
                CartItem cartItem = existingItem.get();
                int newQuantity = cartItem.getQuantity() + quantity;
                if (newQuantity > product.getQuantity()) {
                    return false;
                }
                cartItem.setQuantity(newQuantity);
                cartItemRepository.save(cartItem);
            } else {
                CartItem cartItem = CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(quantity)
                        .build();
                cartItemRepository.save(cartItem);
            }
        } else {
            List<CartItemDTO> cart = getCart(session);
            for (CartItemDTO item : cart) {
                if (item.getProductId().equals(productId)) {
                    int newQuantity = item.getQuantity() + quantity;
                    if (newQuantity > product.getQuantity()) {
                        return false;
                    }
                    item.setQuantity(newQuantity);
                    session.setAttribute(CART_SESSION_KEY, cart);
                    return true;
                }
            }
            CartItemDTO item = CartItemDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .image(product.getImage())
                    .quantity(quantity)
                    .build();
            cart.add(item);
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return true;
    }

    // Cập nhật số lượng
    public boolean updateCartItem(HttpSession session, Authentication authentication, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .filter(p -> p.getIsDelete() == 0)
                .orElse(null);
        if (product == null || quantity <= 0 || quantity > product.getQuantity()) {
            return false;
        }

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Customer> customerOpt = customerRepository.findByUsername(username);
            if (customerOpt.isEmpty()) {
                return false;
            }
            Optional<Cart> cartOpt = cartRepository.findByCustomerId(customerOpt.get().getId());
            if (cartOpt.isPresent()) {
                Optional<CartItem> itemOpt = cartItemRepository.findByCartId(cartOpt.get().getId())
                        .stream()
                        .filter(ci -> ci.getProduct().getId().equals(productId))
                        .findFirst();
                if (itemOpt.isPresent()) {
                    CartItem cartItem = itemOpt.get();
                    cartItem.setQuantity(quantity);
                    cartItemRepository.save(cartItem);
                    return true;
                }
            }
            return false;
        } else {
            List<CartItemDTO> cart = getCart(session);
            for (CartItemDTO item : cart) {
                if (item.getProductId().equals(productId)) {
                    item.setQuantity(quantity);
                    session.setAttribute(CART_SESSION_KEY, cart);
                    return true;
                }
            }
            return false;
        }
    }

    // Xóa sản phẩm
    public void removeCartItem(HttpSession session, Authentication authentication, Long productId) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Customer> customerOpt = customerRepository.findByUsername(username);
            if (customerOpt.isPresent()) {
                Optional<Cart> cartOpt = cartRepository.findByCustomerId(customerOpt.get().getId());
                if (cartOpt.isPresent()) {
                    cartItemRepository.findByCartId(cartOpt.get().getId())
                            .stream()
                            .filter(ci -> ci.getProduct().getId().equals(productId))
                            .findFirst()
                            .ifPresent(cartItemRepository::delete);
                }
            }
        } else {
            List<CartItemDTO> cart = getCart(session);
            cart.removeIf(item -> item.getProductId().equals(productId));
            session.setAttribute(CART_SESSION_KEY, cart);
        }
    }

    // Lấy tổng số sản phẩm
    public int getCartItemCount(HttpSession session, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Customer> customerOpt = customerRepository.findByUsername(username);
            if (customerOpt.isPresent()) {
                return cartRepository.findByCustomerId(customerOpt.get().getId())
                        .map(cart -> cartItemRepository.findByCartId(cart.getId())
                                .stream()
                                .mapToInt(CartItem::getQuantity)
                                .sum())
                        .orElse(0);
            }
        }
        return getCart(session).stream().mapToInt(CartItemDTO::getQuantity).sum();
    }

    // Xóa toàn bộ giỏ hàng
    public void clearCart(HttpSession session, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Customer> customerOpt = customerRepository.findByUsername(username);
            if (customerOpt.isPresent()) {
                cartRepository.findByCustomerId(customerOpt.get().getId())
                        .ifPresent(cart -> cartItemRepository.deleteByCartId(cart.getId()));
            }
        }
        session.removeAttribute(CART_SESSION_KEY);
    }
}