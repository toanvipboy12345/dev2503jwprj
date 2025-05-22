package com.devmaster.dev2503jwprj.repository;

import com.devmaster.dev2503jwprj.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Orders findByIdOrders(String idOrders);
}