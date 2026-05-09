package com.cloud_tecnological.mednova.repositories.habito;

import com.cloud_tecnological.mednova.entity.HabitoPacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitoPacienteJpaRepository
        extends JpaRepository<HabitoPacienteEntity, Long> {
}
