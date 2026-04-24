package com.cloud_tecnological.mednova.repositories.pagador;

import com.cloud_tecnological.mednova.entity.PagadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagadorJpaRepository extends JpaRepository<PagadorEntity, Long> {
}
