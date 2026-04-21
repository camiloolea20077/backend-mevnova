package com.cloud_tecnological.mednova.repositories.serviciohabilitado;

import com.cloud_tecnological.mednova.entity.ServicioHabilitadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioHabilitadoJpaRepository extends JpaRepository<ServicioHabilitadoEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en ServicioHabilitadoQueryRepository.
}
