package com.cloud_tecnological.mednova.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_tecnological.mednova.dto.contactotercero.ContactoTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.contactotercero.ContactoTerceroTableDto;
import com.cloud_tecnological.mednova.dto.contactotercero.CreateContactoTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.contactotercero.UpdateContactoTerceroRequestDto;
import com.cloud_tecnological.mednova.services.ContactoTerceroService;
import com.cloud_tecnological.mednova.util.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/third-contacts")
    
public class ContactoTerceroController {

    private final ContactoTerceroService contactoTerceroService;

    public ContactoTerceroController(ContactoTerceroService contactoTerceroService) {
        this.contactoTerceroService = contactoTerceroService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContactoTerceroResponseDto>> create(
            @Valid @RequestBody CreateContactoTerceroRequestDto request) {
        ContactoTerceroResponseDto result = contactoTerceroService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Contacto creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactoTerceroResponseDto>> findById(@PathVariable Long id) {
        ContactoTerceroResponseDto result = contactoTerceroService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @GetMapping("/third/{thirdPartyId}")
    public ResponseEntity<ApiResponse<List<ContactoTerceroTableDto>>> listByThirdParty(
            @PathVariable Long thirdPartyId) {
        List<ContactoTerceroTableDto> result = contactoTerceroService.listByThirdParty(thirdPartyId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactoTerceroResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactoTerceroRequestDto request) {
        ContactoTerceroResponseDto result = contactoTerceroService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Contacto actualizado", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = contactoTerceroService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Contacto activado" : "Contacto inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}
