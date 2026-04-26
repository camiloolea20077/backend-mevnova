package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.atencion.*;
import java.util.List;

public interface AtencionService {
    AtencionResponseDto findByAdmisionId(Long admisionId);
    AtencionResponseDto registrarTriage(Long id, RegistrarTriageRequestDto dto);
    AtencionResponseDto reclasificarTriage(Long id, ReclasificarTriageRequestDto dto);
    List<ColaUrgenciasDto> getColaUrgencias();
}
