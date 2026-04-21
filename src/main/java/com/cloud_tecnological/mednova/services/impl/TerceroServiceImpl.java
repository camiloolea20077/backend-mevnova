package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.tercero.CreateTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.tercero.TerceroResponseDto;
import com.cloud_tecnological.mednova.dto.tercero.TerceroTableDto;
import com.cloud_tecnological.mednova.dto.tercero.UpdateTerceroRequestDto;
import com.cloud_tecnological.mednova.entity.TerceroEntity;
import com.cloud_tecnological.mednova.repositories.tercero.TerceroJpaRepository;
import com.cloud_tecnological.mednova.repositories.tercero.TerceroQueryRepository;
import com.cloud_tecnological.mednova.services.TerceroService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TerceroServiceImpl implements TerceroService {

    private final TerceroJpaRepository   terceroJpaRepository;
    private final TerceroQueryRepository terceroQueryRepository;

    public TerceroServiceImpl(
            TerceroJpaRepository terceroJpaRepository,
            TerceroQueryRepository terceroQueryRepository) {
        this.terceroJpaRepository   = terceroJpaRepository;
        this.terceroQueryRepository = terceroQueryRepository;
    }

    @Override
    @Transactional
    public TerceroResponseDto create(CreateTerceroRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (terceroQueryRepository.existsByDocumento(
                request.getDocumentTypeId(), request.getDocumentNumber(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Ya existe un tercero con ese tipo y número de documento en la empresa");
        }

        TerceroEntity tercero = new TerceroEntity();
        tercero.setEmpresa_id(empresa_id);
        tercero.setTipo_tercero_id(request.getThirdPartyTypeId());
        tercero.setTipo_documento_id(request.getDocumentTypeId());
        tercero.setNumero_documento(request.getDocumentNumber().trim());
        tercero.setDigito_verificacion(request.getVerificationDigit());
        tercero.setPrimer_nombre(request.getFirstName());
        tercero.setSegundo_nombre(request.getSecondName());
        tercero.setPrimer_apellido(request.getFirstLastName());
        tercero.setSegundo_apellido(request.getSecondLastName());
        tercero.setRazon_social(request.getCompanyName());
        tercero.setNombre_completo(buildFullName(request));
        tercero.setFecha_nacimiento(request.getBirthDate());
        tercero.setSexo_id(request.getSexId());
        tercero.setGenero_id(request.getGenderId());
        tercero.setIdentidad_genero_id(request.getGenderIdentityId());
        tercero.setOrientacion_sexual_id(request.getSexualOrientationId());
        tercero.setEstado_civil_id(request.getMaritalStatusId());
        tercero.setNivel_escolaridad_id(request.getEducationLevelId());
        tercero.setOcupacion_id(request.getOccupationId());
        tercero.setPertenencia_etnica_id(request.getEthnicGroupId());
        tercero.setPais_nacimiento_id(request.getBirthCountryId());
        tercero.setMunicipio_nacimiento_id(request.getBirthMunicipalityId());
        tercero.setObservaciones(request.getObservations());
        tercero.setUsuario_creacion(usuario_id);

        TerceroEntity saved = terceroJpaRepository.save(tercero);

        return terceroQueryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el tercero creado"));
    }

    @Override
    public TerceroResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return terceroQueryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado"));
    }

    @Override
    public TerceroResponseDto findByDocument(Long documentTypeId, String documentNumber) {
        Long empresa_id = TenantContext.getEmpresaId();
        return terceroQueryRepository.findByDocument(documentTypeId, documentNumber.trim(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado"));
    }

    @Override
    public PageImpl<TerceroTableDto> listTerceros(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return terceroQueryRepository.listTerceros(pageable, empresa_id);
    }

    @Override
    @Transactional
    public TerceroResponseDto update(Long id, UpdateTerceroRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        TerceroEntity tercero = terceroJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado"));

        if (!empresa_id.equals(tercero.getEmpresa_id()) || tercero.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado");
        }
        if (!Boolean.TRUE.equals(tercero.getActivo())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "No se puede editar un tercero inactivo");
        }

        if (request.getThirdPartyTypeId() != null)  tercero.setTipo_tercero_id(request.getThirdPartyTypeId());
        tercero.setDigito_verificacion(request.getVerificationDigit());
        tercero.setPrimer_nombre(request.getFirstName());
        tercero.setSegundo_nombre(request.getSecondName());
        tercero.setPrimer_apellido(request.getFirstLastName());
        tercero.setSegundo_apellido(request.getSecondLastName());
        tercero.setRazon_social(request.getCompanyName());
        tercero.setNombre_completo(buildFullNameFromUpdate(request, tercero));
        tercero.setFecha_nacimiento(request.getBirthDate());
        tercero.setSexo_id(request.getSexId());
        tercero.setGenero_id(request.getGenderId());
        tercero.setIdentidad_genero_id(request.getGenderIdentityId());
        tercero.setOrientacion_sexual_id(request.getSexualOrientationId());
        tercero.setEstado_civil_id(request.getMaritalStatusId());
        tercero.setNivel_escolaridad_id(request.getEducationLevelId());
        tercero.setOcupacion_id(request.getOccupationId());
        tercero.setPertenencia_etnica_id(request.getEthnicGroupId());
        tercero.setPais_nacimiento_id(request.getBirthCountryId());
        tercero.setMunicipio_nacimiento_id(request.getBirthMunicipalityId());
        tercero.setObservaciones(request.getObservations());
        tercero.setUsuario_modificacion(usuario_id);
        terceroJpaRepository.save(tercero);

        return terceroQueryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el tercero actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        TerceroEntity tercero = terceroJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado"));

        if (!empresa_id.equals(tercero.getEmpresa_id()) || tercero.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado");
        }

        tercero.setActivo(!Boolean.TRUE.equals(tercero.getActivo()));
        tercero.setUsuario_modificacion(usuario_id);
        terceroJpaRepository.save(tercero);
        return tercero.getActivo();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String buildFullName(CreateTerceroRequestDto request) {
        if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
            return request.getCompanyName().trim();
        }
        return Stream.of(
                request.getFirstName(),
                request.getSecondName(),
                request.getFirstLastName(),
                request.getSecondLastName())
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    private String buildFullNameFromUpdate(UpdateTerceroRequestDto request, TerceroEntity existing) {
        String company = request.getCompanyName() != null ? request.getCompanyName() : existing.getRazon_social();
        if (company != null && !company.isBlank()) return company.trim();
        String fn = request.getFirstName()     != null ? request.getFirstName()     : existing.getPrimer_nombre();
        String sn = request.getSecondName()    != null ? request.getSecondName()    : existing.getSegundo_nombre();
        String fl = request.getFirstLastName() != null ? request.getFirstLastName() : existing.getPrimer_apellido();
        String sl = request.getSecondLastName()!= null ? request.getSecondLastName(): existing.getSegundo_apellido();
        return Stream.of(fn, sn, fl, sl)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }
}
