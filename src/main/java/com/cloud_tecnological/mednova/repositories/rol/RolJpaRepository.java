package com.cloud_tecnological.mednova.repositories.rol;

import com.cloud_tecnological.mednova.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolJpaRepository extends JpaRepository<RolEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en RolQueryRepository.
}
