package com.cloud_tecnological.mednova.dto.atencion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarNotasRequestDto {
    private String chiefComplaint;
    private String currentIllness;
    private String medicalHistory;
    private String physicalExam;
    private String analysis;
    private String plan;
    private String observations;
}
