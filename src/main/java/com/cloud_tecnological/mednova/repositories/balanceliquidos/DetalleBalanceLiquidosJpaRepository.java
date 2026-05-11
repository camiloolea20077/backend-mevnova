package com.cloud_tecnological.mednova.repositories.balanceliquidos;

import com.cloud_tecnological.mednova.entity.DetalleBalanceLiquidosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleBalanceLiquidosJpaRepository
        extends JpaRepository<DetalleBalanceLiquidosEntity, Long> {
}
