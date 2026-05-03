package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.concertacionglosa.ConcertacionGlosaResponseDto;
import com.cloud_tecnological.mednova.dto.concertacionglosa.CreateConcertacionGlosaRequestDto;

public interface ConcertacionGlosaService {

    ConcertacionGlosaResponseDto create(CreateConcertacionGlosaRequestDto request);

    ConcertacionGlosaResponseDto findById(Long id);

    ConcertacionGlosaResponseDto findByGloss(Long glossId);
}
