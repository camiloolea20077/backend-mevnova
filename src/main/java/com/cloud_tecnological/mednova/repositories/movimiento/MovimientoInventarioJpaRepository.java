package com.cloud_tecnological.mednova.repositories.movimiento;

import com.cloud_tecnological.mednova.entity.MovimientoInventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioJpaRepository extends JpaRepository<MovimientoInventarioEntity, Long> {
    // Intencionalmente vacío. Filtros van en MovimientoInventarioQueryRepository.
}
