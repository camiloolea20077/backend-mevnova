package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.contactotercero.ContactoTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.contactotercero.ContactoTerceroTableDto;
import com.cloud_tecnological.mednova.dto.contactotercero.CreateContactoTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.contactotercero.UpdateContactoTerceroRequestDto;
import com.cloud_tecnological.mednova.services.ContactoTerceroService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/third-contacts")
@PreAuthorize("isAuthenticated()")
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
