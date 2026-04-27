package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.cita.AppointmentResponseDto;
import com.cloud_tecnological.mednova.dto.cita.AppointmentTableDto;
import com.cloud_tecnological.mednova.dto.cita.CancelAppointmentRequestDto;
import com.cloud_tecnological.mednova.dto.cita.CreateAppointmentRequestDto;
import com.cloud_tecnological.mednova.dto.cita.RescheduleAppointmentRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface CitaService {
    AppointmentResponseDto create(CreateAppointmentRequestDto dto);
    AppointmentResponseDto findById(Long id);
    PageImpl<AppointmentTableDto> listActive(PageableDto<?> request);
    Boolean cancel(Long id, CancelAppointmentRequestDto dto);
    AppointmentResponseDto reschedule(Long id, RescheduleAppointmentRequestDto dto);
}
