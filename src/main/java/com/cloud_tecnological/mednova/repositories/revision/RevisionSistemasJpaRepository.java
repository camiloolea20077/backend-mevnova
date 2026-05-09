package com.cloud_tecnological.mednova.repositories.revision;

import com.cloud_tecnological.mednova.entity.RevisionSistemasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevisionSistemasJpaRepository
        extends JpaRepository<RevisionSistemasEntity, Long> {
}
