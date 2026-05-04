package com.cloud_tecnological.mednova.repositories.compra;

import com.cloud_tecnological.mednova.entity.CompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraJpaRepository extends JpaRepository<CompraEntity, Long> {
    // Intencionalmente vacío. Filtros van en CompraQueryRepository.
}
