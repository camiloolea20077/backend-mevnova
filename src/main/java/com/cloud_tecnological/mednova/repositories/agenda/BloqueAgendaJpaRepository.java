package com.cloud_tecnological.mednova.repositories.agenda;

import com.cloud_tecnological.mednova.entity.BloqueAgendaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BloqueAgendaJpaRepository extends JpaRepository<BloqueAgendaEntity, Long> {

    @Query("SELECT b FROM BloqueAgendaEntity b WHERE b.agenda_id = :agendaId ORDER BY b.dia_semana, b.hora_inicio")
    List<BloqueAgendaEntity> findByAgendaId(@Param("agendaId") Long agendaId);

    @Modifying
    @Query("DELETE FROM BloqueAgendaEntity b WHERE b.agenda_id = :agendaId")
    void deleteByAgendaId(@Param("agendaId") Long agendaId);
}
