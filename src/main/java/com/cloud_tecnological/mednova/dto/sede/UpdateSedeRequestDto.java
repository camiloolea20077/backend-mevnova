package com.cloud_tecnological.mednova.dto.sede;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSedeRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String name;

    @Size(max = 20, message = "El código de habilitación REPS no puede superar 20 caracteres")
    private String repsCode;

    @NotNull(message = "El país es obligatorio")
    private Long countryId;

    @NotNull(message = "El departamento es obligatorio")
    private Long departmentId;

    @NotNull(message = "El municipio es obligatorio")
    private Long municipalityId;

    @Size(max = 300, message = "La dirección no puede superar 300 caracteres")
    private String address;

    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
    private String phone;

    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String email;

    private Boolean isPrincipal;
}
