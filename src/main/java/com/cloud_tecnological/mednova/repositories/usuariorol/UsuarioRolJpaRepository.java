package com.cloud_tecnological.mednova.repositories.usuariorol;

import com.cloud_tecnological.mednova.entity.UsuarioRolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRolJpaRepository extends JpaRepository<UsuarioRolEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en UsuarioRolQueryRepository.
}
