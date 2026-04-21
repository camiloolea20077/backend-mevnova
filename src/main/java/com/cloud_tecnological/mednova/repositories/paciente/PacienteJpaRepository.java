package com.cloud_tecnological.mednova.repositories.paciente;

import com.cloud_tecnological.mednova.entity.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteJpaRepository extends JpaRepository<PacienteEntity, Long> {
}
