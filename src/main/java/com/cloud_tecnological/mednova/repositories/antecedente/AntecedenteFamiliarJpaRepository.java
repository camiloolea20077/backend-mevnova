package com.cloud_tecnological.mednova.repositories.antecedente;

import com.cloud_tecnological.mednova.entity.AntecedenteFamiliarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AntecedenteFamiliarJpaRepository
        extends JpaRepository<AntecedenteFamiliarEntity, Long> {
}
