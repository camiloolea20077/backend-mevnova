package com.cloud_tecnological.mednova.repositories.solicitudmedicamento;

import com.cloud_tecnological.mednova.entity.DetalleSolicitudMedicamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleSolicitudMedicamentoJpaRepository extends JpaRepository<DetalleSolicitudMedicamentoEntity, Long> {
    // Intencionalmente vacío.
}
