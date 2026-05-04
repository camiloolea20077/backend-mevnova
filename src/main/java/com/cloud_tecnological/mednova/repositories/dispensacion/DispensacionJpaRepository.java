package com.cloud_tecnological.mednova.repositories.dispensacion;

import com.cloud_tecnological.mednova.entity.DispensacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispensacionJpaRepository extends JpaRepository<DispensacionEntity, Long> {
    // Intencionalmente vacío. Filtros van en DispensacionQueryRepository.
}
