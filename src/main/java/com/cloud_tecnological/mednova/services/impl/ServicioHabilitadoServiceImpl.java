package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.serviciohabilitado.CreateServicioHabilitadoRequestDto;
import com.cloud_tecnological.mednova.dto.serviciohabilitado.ServicioHabilitadoResponseDto;
import com.cloud_tecnological.mednova.dto.serviciohabilitado.ServicioHabilitadoTableDto;
import com.cloud_tecnological.mednova.dto.serviciohabilitado.UpdateServicioHabilitadoRequestDto;
import com.cloud_tecnological.mednova.entity.ServicioHabilitadoEntity;
import com.cloud_tecnological.mednova.repositories.sede.SedeQueryRepository;
import com.cloud_tecnological.mednova.repositories.serviciohabilitado.ServicioHabilitadoJpaRepository;
import com.cloud_tecnological.mednova.repositories.serviciohabilitado.ServicioHabilitadoQueryRepository;
import com.cloud_tecnological.mednova.services.ServicioHabilitadoService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ServicioHabilitadoServiceImpl implements ServicioHabilitadoService {

    private final ServicioHabilitadoJpaRepository   jpaRepository;
    private final ServicioHabilitadoQueryRepository queryRepository;
    private final SedeQueryRepository               sedeQueryRepository;

    public ServicioHabilitadoServiceImpl(
            ServicioHabilitadoJpaRepository jpaRepository,
            ServicioHabilitadoQueryRepository queryRepository,
            SedeQueryRepository sedeQueryRepository) {
        this.jpaRepository       = jpaRepository;
        this.queryRepository     = queryRepository;
        this.sedeQueryRepository = sedeQueryRepository;
    }

    @Override
    @Transactional
    public ServicioHabilitadoResponseDto create(CreateServicioHabilitadoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (!sedeQueryRepository.existsActiveByIdAndEmpresa(request.getSedeId(), empresa_id)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La sede no existe o no pertenece a la empresa");
        }
        if (queryRepository.existsActiveBySedeAndCode(request.getSedeId(), empresa_id, request.getServiceCode(), null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe un servicio habilitado con ese código en la sede");
        }

        ServicioHabilitadoEntity entity = new ServicioHabilitadoEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setSede_id(request.getSedeId());
        entity.setCodigo_servicio(request.getServiceCode().toUpperCase());
        entity.setNombre_servicio(request.getServiceName());
        entity.setModalidad(request.getModality().toUpperCase());
        entity.setComplejidad(request.getComplexity().toUpperCase());
        entity.setFecha_habilitacion(request.getEnablementDate());
        entity.setFecha_vencimiento(request.getExpirationDate());
        entity.setResolucion(request.getResolution());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        ServicioHabilitadoEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el servicio creado"));
    }

    @Override
    public ServicioHabilitadoResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Servicio habilitado no encontrado"));
    }

    @Override
    public PageImpl<ServicioHabilitadoTableDto> listServices(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listServices(pageable, empresa_id);
    }

    @Override
    @Transactional
    public ServicioHabilitadoResponseDto update(Long id, UpdateServicioHabilitadoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ServicioHabilitadoEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Servicio habilitado no encontrado"));

        validateTenantAndNotDeleted(entity, empresa_id);

        entity.setNombre_servicio(request.getServiceName());
        entity.setModalidad(request.getModality().toUpperCase());
        entity.setComplejidad(request.getComplexity().toUpperCase());
        entity.setFecha_habilitacion(request.getEnablementDate());
        entity.setFecha_vencimiento(request.getExpirationDate());
        entity.setResolucion(request.getResolution());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el servicio actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ServicioHabilitadoEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Servicio habilitado no encontrado"));

        validateTenantAndNotDeleted(entity, empresa_id);

        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    private void validateTenantAndNotDeleted(ServicioHabilitadoEntity entity, Long empresa_id) {
        if (entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Servicio habilitado no encontrado");
        }
        if (!entity.getEmpresa_id().equals(empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Servicio habilitado no encontrado");
        }
    }
}
