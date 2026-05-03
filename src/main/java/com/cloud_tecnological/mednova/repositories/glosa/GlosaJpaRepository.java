package com.cloud_tecnological.mednova.repositories.glosa;

import com.cloud_tecnological.mednova.entity.GlosaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlosaJpaRepository extends JpaRepository<GlosaEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en GlosaQueryRepository.
}
