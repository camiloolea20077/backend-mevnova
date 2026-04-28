package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.invoice.ApproveInvoiceRequestDto;
import com.cloud_tecnological.mednova.dto.invoice.CreateInvoiceRequestDto;
import com.cloud_tecnological.mednova.dto.invoice.InvoiceResponseDto;
import com.cloud_tecnological.mednova.dto.invoice.InvoiceTableDto;
import com.cloud_tecnological.mednova.dto.invoiceitem.AddInvoiceItemRequestDto;
import com.cloud_tecnological.mednova.dto.invoiceitem.InvoiceItemResponseDto;
import com.cloud_tecnological.mednova.dto.invoiceitem.InvoiceItemTableDto;
import com.cloud_tecnological.mednova.dto.invoiceitem.UpdateInvoiceItemRequestDto;
import com.cloud_tecnological.mednova.services.InvoiceService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponseDto>> create(
            @Valid @RequestBody CreateInvoiceRequestDto request) {
        InvoiceResponseDto result = invoiceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Factura creada exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponseDto>> findById(@PathVariable Long id) {
        InvoiceResponseDto result = invoiceService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<InvoiceTableDto>>> listActive(
            @RequestBody PageableDto<?> request) {
        PageImpl<InvoiceTableDto> result = invoiceService.listActive(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Boolean>> approve(
            @PathVariable Long id,
            @RequestBody ApproveInvoiceRequestDto request) {
        Boolean result = invoiceService.approve(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Factura aprobada exitosamente", false, result));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Boolean>> cancel(@PathVariable Long id) {
        Boolean result = invoiceService.cancel(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Factura anulada exitosamente", false, result));
    }

    // ---- Items ----

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<InvoiceItemResponseDto>> addItem(
            @PathVariable Long id,
            @Valid @RequestBody AddInvoiceItemRequestDto request) {
        InvoiceItemResponseDto result = invoiceService.addItem(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Ítem agregado exitosamente", false, result));
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<InvoiceItemResponseDto>> updateItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateInvoiceItemRequestDto request) {
        InvoiceItemResponseDto result = invoiceService.updateItem(id, itemId, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Ítem actualizado exitosamente", false, result));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteItem(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        Boolean result = invoiceService.deleteItem(id, itemId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Ítem eliminado exitosamente", false, result));
    }

    @PostMapping("/{id}/items/list")
    public ResponseEntity<ApiResponse<PageImpl<InvoiceItemTableDto>>> listItems(
            @PathVariable Long id,
            @RequestBody PageableDto<?> request) {
        PageImpl<InvoiceItemTableDto> result = invoiceService.listItems(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}
