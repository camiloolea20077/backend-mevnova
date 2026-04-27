package com.cloud_tecnological.mednova.repositories.calendario;

import com.cloud_tecnological.mednova.entity.DetalleCalendarioCitaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetalleCalendarioCitaJpaRepository extends JpaRepository<DetalleCalendarioCitaEntity, Long> {

    @Query("SELECT d FROM DetalleCalendarioCitaEntity d WHERE d.calendario_id = :calendarioId ORDER BY d.fecha ASC")
    List<DetalleCalendarioCitaEntity> findByCalendarioId(@Param("calendarioId") Long calendarioId);
}
