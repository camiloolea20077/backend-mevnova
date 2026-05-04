package com.cloud_tecnological.mednova.repositories.lote;

import com.cloud_tecnological.mednova.entity.LoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteJpaRepository extends JpaRepository<LoteEntity, Long> {
    // Intencionalmente vacío. Filtros van en LoteQueryRepository.
}
