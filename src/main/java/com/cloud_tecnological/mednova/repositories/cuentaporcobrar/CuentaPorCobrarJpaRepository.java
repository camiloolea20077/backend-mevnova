package com.cloud_tecnological.mednova.repositories.cuentaporcobrar;

import com.cloud_tecnological.mednova.entity.CuentaPorCobrarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CuentaPorCobrarJpaRepository extends JpaRepository<CuentaPorCobrarEntity, Long> {

    @Query("SELECT c FROM CuentaPorCobrarEntity c WHERE c.factura_id = :facturaId AND c.empresa_id = :empresaId AND c.deleted_at IS NULL")
    Optional<CuentaPorCobrarEntity> findByFacturaIdAndEmpresaId(
            @Param("facturaId") Long facturaId,
            @Param("empresaId") Long empresaId);
}
