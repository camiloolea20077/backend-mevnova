package com.cloud_tecnological.mednova.dto.cobertura;

import com.cloud_tecnological.mednova.dto.contratopaciente.ContratoPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.seguridadsocialpaciente.SeguridadSocialPacienteResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CoberturaVerificacionResponseDto {

    private Long patientId;

    private Boolean hasActiveSocialSecurity;

    private SeguridadSocialPacienteResponseDto socialSecurity;

    private List<ContratoPacienteResponseDto> activeContracts;

    private Long serviceId;

    private Boolean serviceCovered;

    private Boolean requiresAuthorization;

    private Long coveringContractId;

    private String coveringContractNumber;

    private Integer maxQuantity;
}
