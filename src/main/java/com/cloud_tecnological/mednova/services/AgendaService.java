package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.agenda.AgendaResponseDto;
import com.cloud_tecnological.mednova.dto.agenda.AgendaTableDto;
import com.cloud_tecnological.mednova.dto.agenda.CreateAgendaRequestDto;
import com.cloud_tecnological.mednova.dto.traslado.BulkTransferRequestDto;
import com.cloud_tecnological.mednova.dto.traslado.BulkTransferResponseDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface AgendaService {
    AgendaResponseDto create(CreateAgendaRequestDto dto);
    AgendaResponseDto findById(Long id);
    PageImpl<AgendaTableDto> listActivos(PageableDto<?> request);
    Boolean delete(Long id);
    BulkTransferResponseDto bulkTransfer(BulkTransferRequestDto dto);
}
