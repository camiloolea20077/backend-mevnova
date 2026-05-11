package com.cloud_tecnological.mednova.dto.administracionmedicamento;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdverseReactionRequestDto {

    @NotBlank(message = "adverseReaction es obligatorio")
    private String adverseReaction;

    private String observations;
}
