package com.cloud_tecnological.mednova.repositories.plancuidados;

import com.cloud_tecnological.mednova.entity.PlanCuidadosEnfermeriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanCuidadosJpaRepository
        extends JpaRepository<PlanCuidadosEnfermeriaEntity, Long> {
}
