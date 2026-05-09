package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.habito.CreateHabitoPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteFilterParams;
import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteTableDto;
import com.cloud_tecnological.mednova.dto.habito.UpdateHabitoPacienteRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface HabitoPacienteService {

    HabitoPacienteResponseDto create(CreateHabitoPacienteRequestDto request);

    HabitoPacienteResponseDto update(Long id, UpdateHabitoPacienteRequestDto request);

    HabitoPacienteResponseDto findById(Long id);

    PageImpl<HabitoPacienteTableDto> list(PageableDto<HabitoPacienteFilterParams> pageable);

    HabitoPacienteResponseDto setActive(Long id, boolean active);

    void softDelete(Long id);
}
