package com.cloud_tecnological.mednova.repositories.adjuntoclinico;

import com.cloud_tecnological.mednova.entity.AdjuntoClinicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdjuntoClinicoJpaRepository
        extends JpaRepository<AdjuntoClinicoEntity, Long> {
}
