package com.cloud_tecnological.mednova.repositories.stock;

import com.cloud_tecnological.mednova.entity.StockLoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLoteJpaRepository extends JpaRepository<StockLoteEntity, Long> {
    // Intencionalmente vacío. Filtros van en StockLoteQueryRepository.
}
