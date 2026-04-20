package com.cloud_tecnological.mednova.dto.empresa;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmpresaTableDto {

    private Long id;
    private String code;
    private String nit;
    private String businessName;
    private String tradeName;
    private String phone;
    private String email;
    private Boolean active;
}
