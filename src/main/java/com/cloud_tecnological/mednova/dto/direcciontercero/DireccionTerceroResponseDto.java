package com.cloud_tecnological.mednova.dto.direcciontercero;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DireccionTerceroResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long thirdPartyId;
    private String addressType;
    private Long residenceZoneId;
    private Long countryId;
    private Long departmentId;
    private String departmentName;
    private Long municipalityId;
    private String municipalityName;
    private String address;
    private String neighborhood;
    private String postalCode;
    private String reference;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isPrincipal;
    private Boolean active;
    private LocalDateTime createdAt;
}
