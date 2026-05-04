package com.cloud_tecnological.mednova.repositories.ajuste;

import com.cloud_tecnological.mednova.entity.AjusteInventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AjusteInventarioJpaRepository extends JpaRepository<AjusteInventarioEntity, Long> {
    // Intencionalmente vacío. Filtros van en AjusteInventarioQueryRepository.
}
