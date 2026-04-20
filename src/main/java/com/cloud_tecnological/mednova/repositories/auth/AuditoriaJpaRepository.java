package com.cloud_tecnological.mednova.repositories.auth;

import com.cloud_tecnological.mednova.entity.AuditoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaJpaRepository extends JpaRepository<AuditoriaEntity, Long> {
    // Intencionalmente vacío. Solo se usa para escritura (save).
}
