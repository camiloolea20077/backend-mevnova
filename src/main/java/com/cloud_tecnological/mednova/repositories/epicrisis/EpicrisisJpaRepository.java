package com.cloud_tecnological.mednova.repositories.epicrisis;

import com.cloud_tecnological.mednova.entity.EpicrisisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpicrisisJpaRepository
        extends JpaRepository<EpicrisisEntity, Long> {
}
