package com.devmaster.dev2503jwprj.repository;

import com.devmaster.dev2503jwprj.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByProductId(Long productId);
    List<InventoryTransaction> findByProductIdAndTransactionType(Long productId, String transactionType);
}