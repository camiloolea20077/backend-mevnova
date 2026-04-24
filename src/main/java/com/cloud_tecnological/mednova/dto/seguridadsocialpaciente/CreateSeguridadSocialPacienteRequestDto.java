package com.cloud_tecnological.mednova.dto.seguridadsocialpaciente;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateSeguridadSocialPacienteRequestDto {

    @NotNull(message = "El pagador es obligatorio")
    private Long payerId;

    private Long regimenId;

    private Long affiliationCategoryId;

    private Long affiliationTypeId;

    private String affiliationNumber;

    private Long cotizanteThirdPartyId;

    private Boolean isBeneficiary;

    private LocalDate affiliationDate;

    @NotNull(message = "La fecha de vigencia desde es obligatoria")
    private LocalDate validFrom;

    private LocalDate validUntil;

    private Boolean current;

    private String observations;
}
