package com.devmaster.dev2503jwprj.controller;

import com.devmaster.dev2503jwprj.dto.OrderResultDTO;
import com.devmaster.dev2503jwprj.entity.*;
import com.devmaster.dev2503jwprj.repository.*;
import com.devmaster.dev2503jwprj.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private TransportMethodRepository transportMethodRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @GetMapping("/checkout")
    public String showCheckoutForm(Authentication authentication, Model model) {
        String username = authentication.getName();
        logger.info("User {} accessing /orders/checkout", username);

        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        List<CartItem> cartItems = cartRepository.findByCartCustomerId(customer.getId());
        if (cartItems.isEmpty()) {
            logger.warn("Cart is empty for customer: {}", customer.getId());
            model.addAttribute("error", "Giỏ hàng của bạn đang trống!");
            return "home/error";
        }

        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAll();
        List<TransportMethod> transportMethods = transportMethodRepository.findByIsDeleteFalse();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("transportMethods", transportMethods);
        model.addAttribute("customer", customer);
        return "home/checkout";
    }

    @PostMapping("/submit")
    public String submitOrder(
            @RequestParam Long paymentMethodId,
            @RequestParam Long transportMethodId,
            @RequestParam String nameReceiver,
            @RequestParam String address,
            @RequestParam String email,
            @RequestParam String phone,
            Authentication authentication) {
        logger.info("Received /submit request - paymentMethodId: {}, transportMethodId: {}, nameReceiver: {}, address: {}, email: {}, phone: {}",
                paymentMethodId, transportMethodId, nameReceiver, address, email, phone);

        String username = authentication.getName();
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        Orders order = orderService.createOrder(customer.getId(), paymentMethodId, transportMethodId, nameReceiver, address, email, phone);
        logger.info("Order created with ID: {}", order.getIdOrders());

        return "redirect:/orders/success?orderId=" + order.getIdOrders();
    }

    @GetMapping("/success")
    public String showOrderSuccess(
            @RequestParam(value = "orderId", required = false) String orderId,
            Model model) {
        logger.info("Received /success request with orderId: {}", orderId);
        if (orderId == null) {
            logger.error("Order ID is missing in /success request");
            model.addAttribute("error", "Không tìm thấy mã đơn hàng");
            return "home/error";
        }

        Orders order = ordersRepository.findByIdOrders(orderId);
        if (order == null) {
            logger.error("Order not found with id: {}", orderId);
            model.addAttribute("error", "Đơn hàng không tồn tại");
            return "home/error";
        }

        model.addAttribute("order", mapToOrderResultDTO(order));
        return "home/order-success";
    }

    private OrderResultDTO mapToOrderResultDTO(Orders order) {
        OrderResultDTO dto = new OrderResultDTO();
        dto.setIdOrders(order.getIdOrders());
        dto.setOrdersDate(order.getOrdersDate());
        dto.setTotalMoney(order.getTotalMoney());
        dto.setPaymentMethodName(order.getPaymentMethod() != null ? order.getPaymentMethod().getName() : "N/A");
        dto.setTransportMethodName(order.getTransportMethod() != null ? order.getTransportMethod().getName() : "N/A");
        dto.setNameReceiver(order.getNameReceiver());
        dto.setAddress(order.getAddress());
        dto.setEmail(order.getEmail());
        dto.setPhone(order.getPhone());
        dto.setPaymentStatus("SUCCESS");
        return dto;
    }
}