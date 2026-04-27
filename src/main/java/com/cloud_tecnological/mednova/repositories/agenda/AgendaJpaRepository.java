package com.cloud_tecnological.mednova.repositories.agenda;

import com.cloud_tecnological.mednova.entity.AgendaProfesionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaJpaRepository extends JpaRepository<AgendaProfesionalEntity, Long> {
}
