package com.cloud_tecnological.mednova.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PreAuthResponseDto {
    private String preAuthToken;
    private String companyName;
    private String logoUrl;
    private Long expiresInSeconds;
}
