package com.cloud_tecnological.mednova.repositories.bodega;

import com.cloud_tecnological.mednova.entity.BodegaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodegaJpaRepository extends JpaRepository<BodegaEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en BodegaQueryRepository.
}
