package com.cloud_tecnological.mednova.repositories.rips;

import com.cloud_tecnological.mednova.entity.RipsEncabezadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RipsEncabezadoJpaRepository extends JpaRepository<RipsEncabezadoEntity, Long> {

    @Query("SELECT r FROM RipsEncabezadoEntity r WHERE r.factura_id = :facturaId AND r.empresa_id = :empresaId AND r.activo = true")
    Optional<RipsEncabezadoEntity> findByFacturaIdAndEmpresaId(
            @Param("facturaId") Long facturaId,
            @Param("empresaId") Long empresaId);
}
