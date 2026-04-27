package com.cloud_tecnological.mednova.repositories.prescripcion;

import com.cloud_tecnological.mednova.entity.PrescripcionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescripcionJpaRepository extends JpaRepository<PrescripcionEntity, Long> {
}
