package com.cloud_tecnological.mednova.repositories.usuario;

import com.cloud_tecnological.mednova.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en UsuarioQueryRepository.
}
