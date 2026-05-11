package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceItemRequestDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceItemResponseDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosFilterParams;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosResponseDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosTableDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.CreateBalanceLiquidosRequestDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.UpdateBalanceLiquidosRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface BalanceLiquidosService {

    BalanceLiquidosResponseDto create(CreateBalanceLiquidosRequestDto request);

    BalanceLiquidosResponseDto update(Long id, UpdateBalanceLiquidosRequestDto request);

    BalanceLiquidosResponseDto findById(Long id);

    PageImpl<BalanceLiquidosTableDto> list(PageableDto<BalanceLiquidosFilterParams> pageable);

    /** Agregar un detalle al balance (recalcula totales). */
    BalanceItemResponseDto addItem(Long balanceId, BalanceItemRequestDto request);

    /** Listar items del balance. */
    List<BalanceItemResponseDto> listItems(Long balanceId);

    /** Eliminar lógicamente un item (recalcula totales). */
    void deleteItem(Long balanceId, Long itemId);

    void softDelete(Long id);
}
