package com.cloud_tecnological.mednova.repositories.rol;

import com.cloud_tecnological.mednova.entity.RolPermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolPermisoJpaRepository extends JpaRepository<RolPermisoEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en RolQueryRepository.
}
