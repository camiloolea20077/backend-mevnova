package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.vacuna.CreateVacunaPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.vacuna.UpdateVacunaPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteFilterParams;
import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface VacunaPacienteService {

    VacunaPacienteResponseDto create(CreateVacunaPacienteRequestDto request);

    VacunaPacienteResponseDto update(Long id, UpdateVacunaPacienteRequestDto request);

    VacunaPacienteResponseDto findById(Long id);

    PageImpl<VacunaPacienteTableDto> list(PageableDto<VacunaPacienteFilterParams> pageable);

    VacunaPacienteResponseDto setActive(Long id, boolean active);

    void softDelete(Long id);
}
