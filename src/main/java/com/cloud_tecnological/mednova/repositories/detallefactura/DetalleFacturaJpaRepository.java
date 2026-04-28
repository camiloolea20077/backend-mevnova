package com.cloud_tecnological.mednova.repositories.detallefactura;

import com.cloud_tecnological.mednova.entity.DetalleFacturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetalleFacturaJpaRepository extends JpaRepository<DetalleFacturaEntity, Long> {

    @Query("SELECT d FROM DetalleFacturaEntity d WHERE d.factura_id = :facturaId AND d.empresa_id = :empresaId AND d.activo = true")
    List<DetalleFacturaEntity> findActiveByFacturaIdAndEmpresaId(
            @Param("facturaId") Long facturaId,
            @Param("empresaId") Long empresaId);
}
