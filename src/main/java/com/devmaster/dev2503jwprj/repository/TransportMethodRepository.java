package com.devmaster.dev2503jwprj.repository;

import com.devmaster.dev2503jwprj.entity.TransportMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportMethodRepository extends JpaRepository<TransportMethod, Long> {

    @Query("SELECT t FROM TransportMethod t WHERE (LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND t.isDelete = 0")
    List<TransportMethod> searchByName(@Param("keyword") String keyword);

    List<TransportMethod> findByIsDeleteFalse();

    Optional<TransportMethod> findByNameAndIsDeleteFalse(String name);
}