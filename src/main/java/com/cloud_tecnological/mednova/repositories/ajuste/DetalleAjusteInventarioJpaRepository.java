package com.cloud_tecnological.mednova.repositories.ajuste;

import com.cloud_tecnological.mednova.entity.DetalleAjusteInventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleAjusteInventarioJpaRepository extends JpaRepository<DetalleAjusteInventarioEntity, Long> {
    // Intencionalmente vacío. Filtros van en AjusteInventarioQueryRepository.
}
