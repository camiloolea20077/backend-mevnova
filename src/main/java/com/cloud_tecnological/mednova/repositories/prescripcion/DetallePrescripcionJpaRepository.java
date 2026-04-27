package com.cloud_tecnological.mednova.repositories.prescripcion;

import com.cloud_tecnological.mednova.entity.DetallePrescripcionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePrescripcionJpaRepository extends JpaRepository<DetallePrescripcionEntity, Long> {
}
