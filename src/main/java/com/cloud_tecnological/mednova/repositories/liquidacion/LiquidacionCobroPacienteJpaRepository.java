package com.cloud_tecnological.mednova.repositories.liquidacion;

import com.cloud_tecnological.mednova.entity.LiquidacionCobroPacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LiquidacionCobroPacienteJpaRepository extends JpaRepository<LiquidacionCobroPacienteEntity, Long> {

    @Query("SELECT l FROM LiquidacionCobroPacienteEntity l WHERE l.id = :id AND l.empresa_id = :empresaId AND l.activo = true AND l.deleted_at IS NULL")
    Optional<LiquidacionCobroPacienteEntity> findActiveByIdAndEmpresaId(
            @Param("id") Long id,
            @Param("empresaId") Long empresaId);
}
