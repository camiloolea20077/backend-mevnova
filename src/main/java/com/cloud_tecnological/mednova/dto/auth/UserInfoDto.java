package com.cloud_tecnological.mednova.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserInfoDto {
    private Long id;
    private String username;
    private Long companyId;
    private Long branchId;
    private String fullName;
    private List<String> roles;
}
