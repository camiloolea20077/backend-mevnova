package com.cloud_tecnological.mednova.repositories.medicacion;

import com.cloud_tecnological.mednova.entity.MedicacionHabitualEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicacionHabitualJpaRepository
        extends JpaRepository<MedicacionHabitualEntity, Long> {
}
