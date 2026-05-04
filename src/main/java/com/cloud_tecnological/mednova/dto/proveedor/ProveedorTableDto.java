package com.cloud_tecnological.mednova.dto.proveedor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ProveedorTableDto {

    private Long id;
    private String code;
    private String thirdPartyDocumentNumber;
    private String thirdPartyName;
    private Integer paymentTermDays;
    private BigDecimal earlyPaymentDiscount;
    private Boolean requiresPurchaseOrder;
    private Boolean active;
}
