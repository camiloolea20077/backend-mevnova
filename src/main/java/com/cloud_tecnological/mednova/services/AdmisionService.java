package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.admision.*;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface AdmisionService {
    AdmisionResponseDto create(CreateAdmisionRequestDto dto);
    AdmisionResponseDto findById(Long id);
    PageImpl<AdmisionTableDto> listActive(PageableDto<?> request);
    Boolean changeStatus(Long id, ChangeAdmisionStatusRequestDto dto);
    Boolean egreso(Long id, EgresoAdmisionRequestDto dto);
}
