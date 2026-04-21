package com.cloud_tecnological.mednova.repositories.rol;

import com.cloud_tecnological.mednova.entity.PermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoJpaRepository extends JpaRepository<PermisoEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en PermisoQueryRepository.
}
