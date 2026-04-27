package com.cloud_tecnological.mednova.repositories.rol;

import com.cloud_tecnological.mednova.entity.RolPermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolPermisoJpaRepository extends JpaRepository<RolPermisoEntity, Long> {

    @Query("SELECT rp FROM RolPermisoEntity rp WHERE rp.rol_id = :rolId")
    List<RolPermisoEntity> findByRolId(@Param("rolId") Long rolId);
}
