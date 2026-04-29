package com.cloud_tecnological.mednova.repositories.rips;

import com.cloud_tecnological.mednova.entity.RipsDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RipsDetalleJpaRepository extends JpaRepository<RipsDetalleEntity, Long> {

    @Query("SELECT d FROM RipsDetalleEntity d WHERE d.rips_encabezado_id = :encabezadoId AND d.empresa_id = :empresaId")
    List<RipsDetalleEntity> findByEncabezadoIdAndEmpresaId(
            @Param("encabezadoId") Long encabezadoId,
            @Param("empresaId") Long empresaId);
}
