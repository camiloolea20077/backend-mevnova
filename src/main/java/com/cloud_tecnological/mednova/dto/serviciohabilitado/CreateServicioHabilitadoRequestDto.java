package com.cloud_tecnological.mednova.dto.serviciohabilitado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateServicioHabilitadoRequestDto {

    @NotNull(message = "La sede es obligatoria")
    private Long sedeId;

    @NotBlank(message = "El código del servicio es obligatorio")
    @Size(max = 20, message = "El código no puede superar 20 caracteres")
    private String serviceCode;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String serviceName;

    @NotBlank(message = "La modalidad es obligatoria")
    private String modality;

    @NotBlank(message = "La complejidad es obligatoria")
    private String complexity;

    @NotNull(message = "La fecha de habilitación es obligatoria")
    private LocalDate enablementDate;

    private LocalDate expirationDate;

    @Size(max = 50, message = "La resolución no puede superar 50 caracteres")
    private String resolution;

    private String observations;
}
