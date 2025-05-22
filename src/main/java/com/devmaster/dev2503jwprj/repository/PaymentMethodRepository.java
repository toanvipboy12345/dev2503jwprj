package com.devmaster.dev2503jwprj.repository;

import com.devmaster.dev2503jwprj.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
}