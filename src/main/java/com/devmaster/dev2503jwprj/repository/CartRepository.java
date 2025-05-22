package com.devmaster.dev2503jwprj.repository;

import com.devmaster.dev2503jwprj.entity.Cart;
import com.devmaster.dev2503jwprj.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomerId(Long customerId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.customer.id = :customerId")
    List<CartItem> findByCartCustomerId(Long customerId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.customer.id = :customerId")
    void deleteByCartCustomerId(Long customerId);
}