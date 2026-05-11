package com.cloud_tecnological.mednova.repositories.notaenfermeria;

import com.cloud_tecnological.mednova.entity.NotaEnfermeriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotaEnfermeriaJpaRepository
        extends JpaRepository<NotaEnfermeriaEntity, Long> {
}
