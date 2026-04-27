package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.diagnostico.CatalogoDiagnosticoSearchDto;
import com.cloud_tecnological.mednova.dto.diagnostico.CreateDiagnosticoRequestDto;
import com.cloud_tecnological.mednova.dto.diagnostico.DiagnosticoResponseDto;

import java.util.List;

public interface DiagnosticoService {
    DiagnosticoResponseDto create(CreateDiagnosticoRequestDto dto);
    List<DiagnosticoResponseDto> findByAtencionId(Long atencionId);
    Boolean delete(Long id);
    List<CatalogoDiagnosticoSearchDto> searchCatalogo(String search);
}
