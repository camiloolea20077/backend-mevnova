package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.medicacion.CreateMedicacionHabitualRequestDto;
import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualFilterParams;
import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualResponseDto;
import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualTableDto;
import com.cloud_tecnological.mednova.dto.medicacion.UpdateMedicacionHabitualRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface MedicacionHabitualService {

    MedicacionHabitualResponseDto create(CreateMedicacionHabitualRequestDto request);

    MedicacionHabitualResponseDto update(Long id, UpdateMedicacionHabitualRequestDto request);

    MedicacionHabitualResponseDto findById(Long id);

    PageImpl<MedicacionHabitualTableDto> list(PageableDto<MedicacionHabitualFilterParams> pageable);

    /** Marca el medicamento como vigente o suspendido (es_activo). */
    MedicacionHabitualResponseDto setCurrentlyTaking(Long id, boolean currentlyTaking);

    MedicacionHabitualResponseDto setActive(Long id, boolean active);

    void softDelete(Long id);
}
