package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.contactotercero.ContactoTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.contactotercero.ContactoTerceroTableDto;
import com.cloud_tecnological.mednova.dto.contactotercero.CreateContactoTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.contactotercero.UpdateContactoTerceroRequestDto;

import java.util.List;

public interface ContactoTerceroService {

    ContactoTerceroResponseDto create(CreateContactoTerceroRequestDto request);

    ContactoTerceroResponseDto findById(Long id);

    List<ContactoTerceroTableDto> listByThirdParty(Long thirdPartyId);

    ContactoTerceroResponseDto update(Long id, UpdateContactoTerceroRequestDto request);

    Boolean toggleActive(Long id);
}
