package com.cloud_tecnological.mednova.repositories.balanceliquidos;

import com.cloud_tecnological.mednova.entity.BalanceLiquidosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceLiquidosJpaRepository
        extends JpaRepository<BalanceLiquidosEntity, Long> {
}
