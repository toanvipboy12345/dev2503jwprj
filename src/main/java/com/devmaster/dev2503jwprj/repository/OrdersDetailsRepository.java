package com.devmaster.dev2503jwprj.repository;

import com.devmaster.dev2503jwprj.entity.OrdersDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersDetailsRepository extends JpaRepository<OrdersDetails, Long> {
}