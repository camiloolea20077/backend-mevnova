package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.cobertura.CoberturaVerificacionResponseDto;
import com.cloud_tecnological.mednova.dto.contratopaciente.ContratoPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.seguridadsocialpaciente.SeguridadSocialPacienteResponseDto;
import com.cloud_tecnological.mednova.repositories.cobertura.CoberturaVerificacionQueryRepository;
import com.cloud_tecnological.mednova.repositories.contratopaciente.ContratoPacienteQueryRepository;
import com.cloud_tecnological.mednova.repositories.paciente.PacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.seguridadsocialpaciente.SeguridadSocialPacienteQueryRepository;
import com.cloud_tecnological.mednova.services.CoberturaVerificacionService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CoberturaVerificacionServiceImpl implements CoberturaVerificacionService {

    private final PacienteJpaRepository                  pacienteJpaRepository;
    private final SeguridadSocialPacienteQueryRepository ssQueryRepository;
    private final ContratoPacienteQueryRepository        cpQueryRepository;
    private final CoberturaVerificacionQueryRepository   coberturaQueryRepository;

    public CoberturaVerificacionServiceImpl(
            PacienteJpaRepository pacienteJpaRepository,
            SeguridadSocialPacienteQueryRepository ssQueryRepository,
            ContratoPacienteQueryRepository cpQueryRepository,
            CoberturaVerificacionQueryRepository coberturaQueryRepository) {
        this.pacienteJpaRepository  = pacienteJpaRepository;
        this.ssQueryRepository      = ssQueryRepository;
        this.cpQueryRepository      = cpQueryRepository;
        this.coberturaQueryRepository = coberturaQueryRepository;
    }

    @Override
    public CoberturaVerificacionResponseDto verify(Long patientId, Long serviceId) {
        Long empresa_id = TenantContext.getEmpresaId();

        pacienteJpaRepository.findById(patientId)
                .filter(p -> empresa_id.equals(p.getEmpresa_id()) && p.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado en la empresa"));

        List<SeguridadSocialPacienteResponseDto> ssList = ssQueryRepository.listByPaciente(patientId, empresa_id);
        SeguridadSocialPacienteResponseDto activeSS = ssList.stream()
                .filter(ss -> Boolean.TRUE.equals(ss.getCurrent()))
                .findFirst()
                .orElse(null);

        List<ContratoPacienteResponseDto> activeContracts = cpQueryRepository.listByPaciente(patientId, empresa_id)
                .stream()
                .filter(cp -> Boolean.TRUE.equals(cp.getCurrent()))
                .toList();

        CoberturaVerificacionResponseDto.CoberturaVerificacionResponseDtoBuilder builder =
                CoberturaVerificacionResponseDto.builder()
                        .patientId(patientId)
                        .hasActiveSocialSecurity(activeSS != null)
                        .socialSecurity(activeSS)
                        .activeContracts(activeContracts)
                        .serviceId(serviceId);

        if (serviceId != null) {
            Optional<Map<String, Object>> coverage = coberturaQueryRepository
                    .findCoverageForService(patientId, serviceId, empresa_id);
            if (coverage.isPresent()) {
                Map<String, Object> row = coverage.get();
                builder.serviceCovered(true)
                        .requiresAuthorization((Boolean) row.get("requiere_autorizacion"))
                        .coveringContractId(toLong(row.get("contrato_id")))
                        .coveringContractNumber((String) row.get("contrato_numero"))
                        .maxQuantity(row.get("cantidad_maxima") == null ? null : ((Number) row.get("cantidad_maxima")).intValue());
            } else {
                builder.serviceCovered(false)
                        .requiresAuthorization(false);
            }
        }

        return builder.build();
    }

    private Long toLong(Object v) { return v == null ? null : ((Number) v).longValue(); }
}
