package com.cloud_tecnological.mednova.repositories.administracionmedicamento;

import com.cloud_tecnological.mednova.entity.AdministracionMedicamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministracionMedicamentoJpaRepository
        extends JpaRepository<AdministracionMedicamentoEntity, Long> {
}
