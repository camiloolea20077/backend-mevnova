package com.cloud_tecnological.mednova.repositories.empresa;

import com.cloud_tecnological.mednova.entity.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en EmpresaQueryRepository.
}
