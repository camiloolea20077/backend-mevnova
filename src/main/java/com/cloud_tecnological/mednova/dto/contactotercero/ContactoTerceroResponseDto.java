package com.cloud_tecnological.mednova.dto.contactotercero;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ContactoTerceroResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long thirdPartyId;
    private Long contactTypeId;
    private String contactTypeName;
    private String value;
    private Boolean isPrincipal;
    private Boolean acceptsNotifications;
    private Boolean active;
    private LocalDateTime createdAt;
}
