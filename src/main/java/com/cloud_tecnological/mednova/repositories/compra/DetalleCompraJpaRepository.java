package com.cloud_tecnological.mednova.repositories.compra;

import com.cloud_tecnological.mednova.entity.DetalleCompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleCompraJpaRepository extends JpaRepository<DetalleCompraEntity, Long> {
    // Intencionalmente vacío. Filtros van en CompraQueryRepository.
}
