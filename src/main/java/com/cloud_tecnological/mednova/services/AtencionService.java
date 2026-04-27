package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.atencion.*;
import com.cloud_tecnological.mednova.dto.ordenclinica.OrdenClinicaResponseDto;
import java.util.List;

public interface AtencionService {
    AtencionResponseDto findByAdmisionId(Long admisionId);
    AtencionResponseDto registrarTriage(Long id, RegistrarTriageRequestDto dto);
    AtencionResponseDto reclasificarTriage(Long id, ReclasificarTriageRequestDto dto);
    List<ColaUrgenciasDto> getColaUrgencias();
    ConsolaAtencionDto getConsola(Long id);
    AtencionResponseDto startAttention(Long id);
    AtencionResponseDto actualizarNotas(Long id, ActualizarNotasRequestDto dto);
    AtencionResponseDto cerrarAtencion(Long id, CerrarAtencionRequestDto dto);
    AtencionResponseDto solicitarHospitalizacion(Long id, SolicitarHospitalizacionRequestDto dto);

    // Hospitalización
    AtencionResponseDto assignBed(Long admisionId, AssignBedRequestDto dto);
    AtencionResponseDto registrarNotaIngreso(Long id, NotaIngresoRequestDto dto);
    AtencionResponseDto registrarEvolucion(Long id, EvolucionHospitalariaRequestDto dto);
    List<OrdenClinicaResponseDto> getOrdenesActivas(Long id);
    Boolean registrarEgreso(Long id, EgresoHospitalarioRequestDto dto);
}
