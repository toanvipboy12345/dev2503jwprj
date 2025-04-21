package com.devmaster.dev2503jwprj.repository;

import com.devmaster.dev2503jwprj.entity.Configurations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationsRepository extends JpaRepository<Configurations, Long> {
    List<Configurations> findByIsDeleteFalse();
    Optional<Configurations> findByNameAndIsDeleteFalse(String name);
}