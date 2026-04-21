package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.contactotercero.ContactoTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.contactotercero.ContactoTerceroTableDto;
import com.cloud_tecnological.mednova.dto.contactotercero.CreateContactoTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.contactotercero.UpdateContactoTerceroRequestDto;
import com.cloud_tecnological.mednova.entity.ContactoTerceroEntity;
import com.cloud_tecnological.mednova.repositories.contactotercero.ContactoTerceroJpaRepository;
import com.cloud_tecnological.mednova.repositories.contactotercero.ContactoTerceroQueryRepository;
import com.cloud_tecnological.mednova.repositories.tercero.TerceroJpaRepository;
import com.cloud_tecnological.mednova.services.ContactoTerceroService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactoTerceroServiceImpl implements ContactoTerceroService {

    private final ContactoTerceroJpaRepository   jpaRepository;
    private final ContactoTerceroQueryRepository queryRepository;
    private final TerceroJpaRepository           terceroJpaRepository;

    public ContactoTerceroServiceImpl(
            ContactoTerceroJpaRepository jpaRepository,
            ContactoTerceroQueryRepository queryRepository,
            TerceroJpaRepository terceroJpaRepository) {
        this.jpaRepository        = jpaRepository;
        this.queryRepository      = queryRepository;
        this.terceroJpaRepository = terceroJpaRepository;
    }

    @Override
    @Transactional
    public ContactoTerceroResponseDto create(CreateContactoTerceroRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validateTerceroPertenece(request.getThirdPartyId(), empresa_id);

        if (Boolean.TRUE.equals(request.getIsPrincipal())) {
            queryRepository.unmarkPrincipal(request.getThirdPartyId(), request.getContactTypeId(), empresa_id);
        }

        ContactoTerceroEntity entity = new ContactoTerceroEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setTercero_id(request.getThirdPartyId());
        entity.setTipo_contacto_id(request.getContactTypeId());
        entity.setValor(request.getValue().trim());
        entity.setEs_principal(Boolean.TRUE.equals(request.getIsPrincipal()));
        entity.setAcepta_notificaciones(request.getAcceptsNotifications() == null || request.getAcceptsNotifications());
        entity.setUsuario_creacion(usuario_id);

        ContactoTerceroEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el contacto creado"));
    }

    @Override
    public ContactoTerceroResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Contacto no encontrado"));
    }

    @Override
    public List<ContactoTerceroTableDto> listByThirdParty(Long thirdPartyId) {
        Long empresa_id = TenantContext.getEmpresaId();
        validateTerceroPertenece(thirdPartyId, empresa_id);
        return queryRepository.listByThirdParty(thirdPartyId, empresa_id);
    }

    @Override
    @Transactional
    public ContactoTerceroResponseDto update(Long id, UpdateContactoTerceroRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ContactoTerceroEntity entity = getValidEntity(id, empresa_id);

        if (Boolean.TRUE.equals(request.getIsPrincipal())) {
            queryRepository.unmarkPrincipal(entity.getTercero_id(), request.getContactTypeId(), empresa_id);
        }

        entity.setTipo_contacto_id(request.getContactTypeId());
        entity.setValor(request.getValue().trim());
        entity.setEs_principal(Boolean.TRUE.equals(request.getIsPrincipal()));
        entity.setAcepta_notificaciones(request.getAcceptsNotifications() == null || request.getAcceptsNotifications());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el contacto actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ContactoTerceroEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    private void validateTerceroPertenece(Long tercero_id, Long empresa_id) {
        terceroJpaRepository.findById(tercero_id)
                .filter(t -> empresa_id.equals(t.getEmpresa_id()) && t.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado en la empresa"));
    }

    private ContactoTerceroEntity getValidEntity(Long id, Long empresa_id) {
        ContactoTerceroEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Contacto no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Contacto no encontrado");
        }
        return entity;
    }
}
