package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.historiaclinica.HCAnamnesisDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCAttachmentDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCEpisodeDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCHeaderDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCMedicationDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCNoteDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCOrderDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCScaleDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCSummaryDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCTimelineEventDto;

import java.util.List;

public interface HistoriaClinicaService {

    HCHeaderDto getHeader(Long patientId);

    HCSummaryDto getSummary(Long patientId);

    List<HCEpisodeDto> getEpisodes(Long patientId);

    HCAnamnesisDto getAnamnesis(Long patientId);

    List<HCNoteDto> getNotes(Long patientId);

    List<HCOrderDto> getOrders(Long patientId);

    HCMedicationDto getMedications(Long patientId);

    List<HCScaleDto> getScales(Long patientId);

    List<HCAttachmentDto> getAttachments(Long patientId);

    List<HCTimelineEventDto> getTimeline(Long patientId);
}
