package com.devmaster.dev2503jwprj.service;

import com.devmaster.dev2503jwprj.entity.PaymentMethod;
import com.devmaster.dev2503jwprj.repository.PaymentMethodRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.findAll().stream()
                .filter(payment -> payment.getIsDelete() == null || payment.getIsDelete() == 0)
                .toList();
    }

    public Optional<PaymentMethod> getPaymentMethodById(Long id) {
        return paymentMethodRepository.findById(id)
                .filter(payment -> payment.getIsDelete() == null || payment.getIsDelete() == 0);
    }

    public void createPaymentMethod(@Valid PaymentMethod paymentMethod) {
        // Kiểm tra trùng lặp dựa trên name (nếu name là duy nhất)
        if (paymentMethodRepository.findAll().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(paymentMethod.getName()) && 
                             (p.getIsDelete() == null || p.getIsDelete() == 0))) {
            throw new IllegalArgumentException("Phương thức thanh toán với tên này đã tồn tại");
        }

        paymentMethod.setCreatedDate(LocalDateTime.now());
        paymentMethod.setIsDelete((byte) 0);
        paymentMethod.setIsActive((byte) 1);
        paymentMethodRepository.save(paymentMethod);
    }

    public void updatePaymentMethod(@Valid PaymentMethod paymentMethod) {
        Optional<PaymentMethod> existingPaymentOpt = paymentMethodRepository.findById(paymentMethod.getId());
        if (!existingPaymentOpt.isPresent()) {
            throw new IllegalArgumentException("Phương thức thanh toán không tồn tại");
        }

        PaymentMethod existingPayment = existingPaymentOpt.get();
        paymentMethod.setCreatedDate(existingPayment.getCreatedDate());
        paymentMethod.setIsDelete(existingPayment.getIsDelete());
        paymentMethod.setUpdatedDate(LocalDateTime.now());
        paymentMethodRepository.save(paymentMethod);
    }

    public void deletePaymentMethod(Long id) {
        Optional<PaymentMethod> paymentOpt = paymentMethodRepository.findById(id);
        if (!paymentOpt.isPresent()) {
            throw new IllegalArgumentException("Phương thức thanh toán không tồn tại");
        }
        PaymentMethod payment = paymentOpt.get();
        if (payment.getIsDelete() == 1) {
            throw new IllegalArgumentException("Phương thức thanh toán đã bị xóa trước đó");
        }
        payment.setIsDelete((byte) 1);
        payment.setUpdatedDate(LocalDateTime.now());
        paymentMethodRepository.save(payment);
    }

    public List<PaymentMethod> searchPaymentMethods(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPaymentMethods();
        }
        return paymentMethodRepository.findAll().stream()
                .filter(payment -> payment.getIsDelete() == 0 &&
                        payment.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }
}