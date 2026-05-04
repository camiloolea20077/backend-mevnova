package com.cloud_tecnological.mednova.repositories.solicitudmedicamento;

import com.cloud_tecnological.mednova.entity.SolicitudMedicamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudMedicamentoJpaRepository extends JpaRepository<SolicitudMedicamentoEntity, Long> {
    // Intencionalmente vacío. Filtros van en SolicitudMedicamentoQueryRepository.
}
