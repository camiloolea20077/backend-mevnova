package com.cloud_tecnological.mednova.repositories.sede;

import com.cloud_tecnological.mednova.entity.SedeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SedeJpaRepository extends JpaRepository<SedeEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en SedeQueryRepository.
}
