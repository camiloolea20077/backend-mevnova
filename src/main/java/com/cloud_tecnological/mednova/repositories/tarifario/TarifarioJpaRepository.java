package com.cloud_tecnological.mednova.repositories.tarifario;

import com.cloud_tecnological.mednova.entity.TarifarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarifarioJpaRepository extends JpaRepository<TarifarioEntity, Long> {
}
