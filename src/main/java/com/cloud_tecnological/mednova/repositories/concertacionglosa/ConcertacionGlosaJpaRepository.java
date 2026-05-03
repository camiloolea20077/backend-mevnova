package com.cloud_tecnological.mednova.repositories.concertacionglosa;

import com.cloud_tecnological.mednova.entity.ConcertacionGlosaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertacionGlosaJpaRepository extends JpaRepository<ConcertacionGlosaEntity, Long> {
    // Intencionalmente vacío. Todo filtro de negocio va en ConcertacionGlosaQueryRepository.
}
