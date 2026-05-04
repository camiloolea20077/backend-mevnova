package com.cloud_tecnological.mednova.repositories.proveedor;

import com.cloud_tecnological.mednova.entity.ProveedorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorJpaRepository extends JpaRepository<ProveedorEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en ProveedorQueryRepository.
}
