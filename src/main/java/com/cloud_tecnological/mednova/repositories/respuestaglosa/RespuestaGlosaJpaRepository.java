package com.cloud_tecnological.mednova.repositories.respuestaglosa;

import com.cloud_tecnological.mednova.entity.RespuestaGlosaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RespuestaGlosaJpaRepository extends JpaRepository<RespuestaGlosaEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en RespuestaGlosaQueryRepository.
}
