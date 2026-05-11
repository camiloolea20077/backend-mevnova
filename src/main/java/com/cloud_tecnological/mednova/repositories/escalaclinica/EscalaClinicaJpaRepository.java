package com.cloud_tecnological.mednova.repositories.escalaclinica;

import com.cloud_tecnological.mednova.entity.EscalaClinicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EscalaClinicaJpaRepository
        extends JpaRepository<EscalaClinicaEntity, Long> {
}
