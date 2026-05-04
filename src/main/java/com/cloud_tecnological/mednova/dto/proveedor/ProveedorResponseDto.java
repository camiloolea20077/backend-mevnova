package com.cloud_tecnological.mednova.dto.proveedor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProveedorResponseDto {

    private Long id;
    private Long thirdPartyId;
    private String thirdPartyDocumentNumber;
    private String thirdPartyName;
    private String code;
    private String accountingAccount;
    private Integer paymentTermDays;
    private BigDecimal earlyPaymentDiscount;
    private Boolean requiresPurchaseOrder;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}
