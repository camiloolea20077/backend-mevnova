package com.cloud_tecnological.mednova.repositories.dispensacion;

import com.cloud_tecnological.mednova.entity.DetalleDispensacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleDispensacionJpaRepository extends JpaRepository<DetalleDispensacionEntity, Long> {
    // Intencionalmente vacío.
}
