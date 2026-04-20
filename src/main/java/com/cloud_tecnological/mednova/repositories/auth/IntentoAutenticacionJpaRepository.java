package com.cloud_tecnological.mednova.repositories.auth;

import com.cloud_tecnological.mednova.entity.IntentoAutenticacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentoAutenticacionJpaRepository extends JpaRepository<IntentoAutenticacionEntity, Long> {
}
