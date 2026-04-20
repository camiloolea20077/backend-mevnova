package com.cloud_tecnological.mednova.repositories.auth;

import com.cloud_tecnological.mednova.entity.HistorialPasswordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialPasswordJpaRepository extends JpaRepository<HistorialPasswordEntity, Long> {
    // Intencionalmente vacío
}
