package com.cloud_tecnological.mednova.repositories.detalleglosa;

import com.cloud_tecnological.mednova.entity.DetalleGlosaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleGlosaJpaRepository extends JpaRepository<DetalleGlosaEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en DetalleGlosaQueryRepository.
}
