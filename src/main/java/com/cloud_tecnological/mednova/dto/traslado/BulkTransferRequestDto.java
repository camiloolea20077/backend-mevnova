package com.cloud_tecnological.mednova.dto.traslado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class BulkTransferRequestDto {

    @NotNull
    private Long agendaOrigenId;

    @NotNull
    private LocalDate fechaOrigen;

    private Long agendaDestinoId;
    private LocalDate fechaDestino;

    @NotBlank
    private String motivo;
}
