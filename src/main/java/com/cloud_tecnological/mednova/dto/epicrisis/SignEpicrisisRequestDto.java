package com.cloud_tecnological.mednova.dto.epicrisis;

import lombok.Getter;
import lombok.Setter;

/**
 * Firma de la epicrisis. CA4: pdfUrl opcional al firmar (puede ya estar guardada).
 */
@Getter
@Setter
public class SignEpicrisisRequestDto {

    private String pdfUrl;
}
