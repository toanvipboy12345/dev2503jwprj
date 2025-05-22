package com.devmaster.dev2503jwprj.service;

import com.devmaster.dev2503jwprj.entity.*;
import com.devmaster.dev2503jwprj.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrdersDetailsRepository ordersDetailsRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private TransportMethodRepository transportMethodRepository;

    @Transactional
    public Orders createOrder(Long customerId, Long paymentMethodId, Long transportMethodId, String nameReceiver, String address, String email, String phone) {
        // Lấy giỏ hàng của khách hàng
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng rỗng");
        }

        // Kiểm tra số lượng sản phẩm
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng");
            }
        }

        // Tạo đơn hàng
        Orders order = Orders.builder()
                .idOrders(UUID.randomUUID().toString().substring(0, 10))
                .ordersDate(LocalDateTime.now())
                .customer(cart.getCustomer())
                .paymentMethod(paymentMethodRepository.findById(paymentMethodId)
                        .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không tồn tại")))
                .transportMethod(transportMethodRepository.findById(transportMethodId)
                        .orElseThrow(() -> new RuntimeException("Phương thức vận chuyển không tồn tại")))
                .nameReceiver(nameReceiver)
                .address(address)
                .email(email)
                .phone(phone)
                .isActive((byte) 1)
                .isDelete((byte) 0)
                .build();

        // Tính tổng tiền
        BigDecimal totalMoney = BigDecimal.ZERO;
        List<OrdersDetails> orderDetailsList = new ArrayList<>();
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            BigDecimal price = product.getPrice();
            int quantity = item.getQuantity();
            BigDecimal total = price.multiply(new BigDecimal(quantity));

            OrdersDetails orderDetail = OrdersDetails.builder()
                    .order(order)
                    .product(product)
                    .price(price)
                    .qty(quantity)
                    .total(total)
                    .returnQty(0)
                    .build();

            orderDetailsList.add(orderDetail);
            totalMoney = totalMoney.add(total);

            // Trừ số lượng sản phẩm
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);
        }

        order.setTotalMoney(totalMoney);
        ordersRepository.save(order);
        ordersDetailsRepository.saveAll(orderDetailsList);

        // Xóa giỏ hàng
        cartItemRepository.deleteByCartId(cart.getId());

        return order;
    }
}