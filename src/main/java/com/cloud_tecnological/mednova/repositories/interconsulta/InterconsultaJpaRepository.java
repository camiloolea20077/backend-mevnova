package com.cloud_tecnological.mednova.repositories.interconsulta;

import com.cloud_tecnological.mednova.entity.InterconsultaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterconsultaJpaRepository
        extends JpaRepository<InterconsultaEntity, Long> {
}
