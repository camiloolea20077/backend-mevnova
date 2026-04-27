package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.diagnostico.CatalogoDiagnosticoSearchDto;
import com.cloud_tecnological.mednova.dto.diagnostico.CreateDiagnosticoRequestDto;
import com.cloud_tecnological.mednova.dto.diagnostico.DiagnosticoResponseDto;
import com.cloud_tecnological.mednova.entity.DiagnosticoAtencionEntity;
import com.cloud_tecnological.mednova.repositories.atencion.AtencionJpaRepository;
import com.cloud_tecnological.mednova.repositories.diagnostico.DiagnosticoAtencionJpaRepository;
import com.cloud_tecnological.mednova.repositories.diagnostico.DiagnosticoAtencionQueryRepository;
import com.cloud_tecnological.mednova.services.DiagnosticoService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import com.cloud_tecnological.mednova.entity.AtencionEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiagnosticoServiceImpl implements DiagnosticoService {

    private final DiagnosticoAtencionJpaRepository diagnosticoJpaRepository;
    private final DiagnosticoAtencionQueryRepository diagnosticoQueryRepository;
    private final AtencionJpaRepository atencionJpaRepository;

    public DiagnosticoServiceImpl(DiagnosticoAtencionJpaRepository diagnosticoJpaRepository,
                                  DiagnosticoAtencionQueryRepository diagnosticoQueryRepository,
                                  AtencionJpaRepository atencionJpaRepository) {
        this.diagnosticoJpaRepository  = diagnosticoJpaRepository;
        this.diagnosticoQueryRepository = diagnosticoQueryRepository;
        this.atencionJpaRepository     = atencionJpaRepository;
    }

    @Override
    @Transactional
    public DiagnosticoResponseDto create(CreateDiagnosticoRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        AtencionEntity atencion = atencionJpaRepository.findById(dto.getAtencionId())
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        validateAtencionTenant(atencion, empresaId);

        if (!diagnosticoQueryRepository.catalogoDiagnosticoExists(dto.getCatalogoDiagnosticoId())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Código diagnóstico no encontrado en el catálogo");
        }

        if ("PRINCIPAL".equals(dto.getTipoDiagnostico())
                && diagnosticoQueryRepository.existsPrincipalByAtencion(dto.getAtencionId(), empresaId)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                "La atención ya tiene un diagnóstico PRINCIPAL. Elimínelo primero o cambie el tipo.");
        }

        DiagnosticoAtencionEntity entity = new DiagnosticoAtencionEntity();
        entity.setEmpresa_id(empresaId);
        entity.setAtencion_id(dto.getAtencionId());
        entity.setCatalogo_diagnostico_id(dto.getCatalogoDiagnosticoId());
        entity.setTipo_diagnostico(dto.getTipoDiagnostico());
        entity.setEs_confirmado(dto.getEsConfirmado() != null ? dto.getEsConfirmado() : false);
        entity.setObservaciones(dto.getObservaciones());

        DiagnosticoAtencionEntity saved = diagnosticoJpaRepository.save(entity);

        return diagnosticoQueryRepository.findByAtencionId(saved.getAtencion_id(), empresaId)
            .stream()
            .filter(d -> d.getId().equals(saved.getId()))
            .findFirst()
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el diagnóstico"));
    }

    @Override
    public List<DiagnosticoResponseDto> findByAtencionId(Long atencionId) {
        Long empresaId = TenantContext.getEmpresaId();
        atencionJpaRepository.findById(atencionId)
            .filter(a -> a.getEmpresa_id().equals(empresaId) && a.getDeleted_at() == null)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada"));

        return diagnosticoQueryRepository.findByAtencionId(atencionId, empresaId);
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Long empresaId = TenantContext.getEmpresaId();

        DiagnosticoAtencionEntity entity = diagnosticoJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Diagnóstico no encontrado"));

        if (!entity.getEmpresa_id().equals(empresaId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Diagnóstico no encontrado");
        }
        if (!entity.getActivo()) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "El diagnóstico ya fue eliminado");
        }

        entity.setActivo(false);
        diagnosticoJpaRepository.save(entity);
        return true;
    }

    @Override
    public List<CatalogoDiagnosticoSearchDto> searchCatalogo(String search) {
        if (search == null || search.trim().length() < 2) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "Ingrese al menos 2 caracteres para buscar en el catálogo");
        }
        return diagnosticoQueryRepository.searchCatalogo(search.trim(), 50);
    }

    private void validateAtencionTenant(AtencionEntity atencion, Long empresaId) {
        if (atencion.getDeleted_at() != null || !atencion.getEmpresa_id().equals(empresaId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada");
        }
    }
}
