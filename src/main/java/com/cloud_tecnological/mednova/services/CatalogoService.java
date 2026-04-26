package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.catalogo.CatalogoResponseDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CatalogoService {
    List<CatalogoResponseDto> findAll(String tableName, Boolean activo);
}