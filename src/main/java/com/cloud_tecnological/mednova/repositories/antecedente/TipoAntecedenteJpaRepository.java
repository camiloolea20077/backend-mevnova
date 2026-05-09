package com.cloud_tecnological.mednova.repositories.antecedente;

import com.cloud_tecnological.mednova.entity.TipoAntecedenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoAntecedenteJpaRepository
        extends JpaRepository<TipoAntecedenteEntity, Long> {
}
