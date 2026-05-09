package com.cloud_tecnological.mednova.repositories.vacuna;

import com.cloud_tecnological.mednova.entity.VacunaPacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacunaPacienteJpaRepository
        extends JpaRepository<VacunaPacienteEntity, Long> {
}
