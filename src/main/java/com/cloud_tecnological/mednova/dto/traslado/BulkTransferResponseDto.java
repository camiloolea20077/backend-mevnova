package com.cloud_tecnological.mednova.dto.traslado;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class BulkTransferResponseDto {
    private Long trasladoId;
    private LocalDate fechaOrigen;
    private LocalDate fechaDestino;
    private Integer totalCitas;
    private Integer citasTrasladadas;
    private Integer citasFallidas;
}
