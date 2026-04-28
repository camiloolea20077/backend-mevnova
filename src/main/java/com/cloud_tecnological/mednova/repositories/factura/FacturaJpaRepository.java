package com.cloud_tecnological.mednova.repositories.factura;

import com.cloud_tecnological.mednova.entity.FacturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaJpaRepository extends JpaRepository<FacturaEntity, Long> {
}
