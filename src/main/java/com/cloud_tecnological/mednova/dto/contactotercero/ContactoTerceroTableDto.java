package com.cloud_tecnological.mednova.dto.contactotercero;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ContactoTerceroTableDto {

    private Long id;
    private String contactTypeName;
    private String value;
    private Boolean isPrincipal;
    private Boolean acceptsNotifications;
    private Boolean active;
}
