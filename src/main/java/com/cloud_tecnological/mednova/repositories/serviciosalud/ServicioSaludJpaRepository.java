package com.cloud_tecnological.mednova.repositories.serviciosalud;

import com.cloud_tecnological.mednova.entity.ServicioSaludEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioSaludJpaRepository extends JpaRepository<ServicioSaludEntity, Long> {
}
