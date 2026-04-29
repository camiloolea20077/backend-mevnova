package com.cloud_tecnological.mednova.repositories.radicacion;

import com.cloud_tecnological.mednova.entity.RadicacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RadicacionJpaRepository extends JpaRepository<RadicacionEntity, Long> {

    @Query("SELECT r FROM RadicacionEntity r WHERE r.id = :id AND r.empresa_id = :empresaId AND r.activo = true AND r.deleted_at IS NULL")
    Optional<RadicacionEntity> findActiveByIdAndEmpresaId(
            @Param("id") Long id,
            @Param("empresaId") Long empresaId);
}
