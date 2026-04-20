package com.cloud_tecnological.mednova.repositories.auth;

import com.cloud_tecnological.mednova.entity.SesionUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionUsuarioJpaRepository extends JpaRepository<SesionUsuarioEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en SesionUsuarioQueryRepository.
}
