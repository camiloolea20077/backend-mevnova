package com.cloud_tecnological.mednova.controller;

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
import com.cloud_tecnological.mednova.services.HistoriaClinicaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clinical-records")
public class HistoriaClinicaController {

    private final HistoriaClinicaService service;

    public HistoriaClinicaController(HistoriaClinicaService service) {
        this.service = service;
    }

    // HU-FASE2-093 CA1+CA4: Encabezado con alergias destacadas
    @GetMapping("/{patientId}/header")
    public ResponseEntity<ApiResponse<HCHeaderDto>> header(@PathVariable Long patientId) {
        HCHeaderDto result = service.getHeader(patientId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-093: Pestaña Resumen
    @GetMapping("/{patientId}/summary")
    public ResponseEntity<ApiResponse<HCSummaryDto>> summary(@PathVariable Long patientId) {
        HCSummaryDto result = service.getSummary(patientId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-093: Pestaña Episodios
    @GetMapping("/{patientId}/episodes")
    public ResponseEntity<ApiResponse<List<HCEpisodeDto>>> episodes(@PathVariable Long patientId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, service.getEpisodes(patientId)));
    }

    // HU-FASE2-093: Pestaña Anamnesis
    @GetMapping("/{patientId}/anamnesis")
    public ResponseEntity<ApiResponse<HCAnamnesisDto>> anamnesis(@PathVariable Long patientId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, service.getAnamnesis(patientId)));
    }

    // HU-FASE2-093: Pestaña Notas
    @GetMapping("/{patientId}/notes")
    public ResponseEntity<ApiResponse<List<HCNoteDto>>> notes(@PathVariable Long patientId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, service.getNotes(patientId)));
    }

    // HU-FASE2-093: Pestaña Órdenes
    @GetMapping("/{patientId}/orders")
    public ResponseEntity<ApiResponse<List<HCOrderDto>>> orders(@PathVariable Long patientId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, service.getOrders(patientId)));
    }

    // HU-FASE2-093: Pestaña Medicamentos
    @GetMapping("/{patientId}/medications")
    public ResponseEntity<ApiResponse<HCMedicationDto>> medications(@PathVariable Long patientId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, service.getMedications(patientId)));
    }

    // HU-FASE2-093: Pestaña Escalas
    @GetMapping("/{patientId}/scales")
    public ResponseEntity<ApiResponse<List<HCScaleDto>>> scales(@PathVariable Long patientId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, service.getScales(patientId)));
    }

    // HU-FASE2-093: Pestaña Adjuntos (oculta confidenciales sin permiso)
    @GetMapping("/{patientId}/attachments")
    public ResponseEntity<ApiResponse<List<HCAttachmentDto>>> attachments(@PathVariable Long patientId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, service.getAttachments(patientId)));
    }

    // HU-FASE2-093 CA5: Línea de tiempo
    @GetMapping("/{patientId}/timeline")
    public ResponseEntity<ApiResponse<List<HCTimelineEventDto>>> timeline(@PathVariable Long patientId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, service.getTimeline(patientId)));
    }
}
