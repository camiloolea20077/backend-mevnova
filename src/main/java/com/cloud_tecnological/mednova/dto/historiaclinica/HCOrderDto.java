package com.cloud_tecnological.mednova.dto.historiaclinica;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class HCOrderDto {

    private Long orderId;
    private String orderNumber;
    private Long encounterId;
    private String status;
    private LocalDateTime createdAt;
    private Integer itemCount;
}
