package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.dispensacion.CancelDispensacionRequestDto;
import com.cloud_tecnological.mednova.dto.dispensacion.CreateDispensacionRequestDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensacionResponseDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensacionTableDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensationSuggestionItemDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface DispensacionService {

    DispensacionResponseDto create(CreateDispensacionRequestDto request);

    DispensacionResponseDto findById(Long id);

    PageImpl<DispensacionTableDto> list(PageableDto<?> pageable);

    DispensacionResponseDto cancel(Long id, CancelDispensacionRequestDto request);

    List<DispensationSuggestionItemDto> getSuggestions(Long prescripcionId, Long bodegaId);
}
