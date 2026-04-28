package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.invoice.ApproveInvoiceRequestDto;
import com.cloud_tecnological.mednova.dto.invoice.CreateInvoiceRequestDto;
import com.cloud_tecnological.mednova.dto.invoice.InvoiceResponseDto;
import com.cloud_tecnological.mednova.dto.invoice.InvoiceTableDto;
import com.cloud_tecnological.mednova.dto.invoiceitem.AddInvoiceItemRequestDto;
import com.cloud_tecnological.mednova.dto.invoiceitem.InvoiceItemResponseDto;
import com.cloud_tecnological.mednova.dto.invoiceitem.InvoiceItemTableDto;
import com.cloud_tecnological.mednova.dto.invoiceitem.UpdateInvoiceItemRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface InvoiceService {
    InvoiceResponseDto create(CreateInvoiceRequestDto dto);
    InvoiceResponseDto findById(Long id);
    PageImpl<InvoiceTableDto> listActive(PageableDto<?> request);
    Boolean approve(Long id, ApproveInvoiceRequestDto dto);
    Boolean cancel(Long id);

    InvoiceItemResponseDto addItem(Long facturaId, AddInvoiceItemRequestDto dto);
    InvoiceItemResponseDto updateItem(Long facturaId, Long itemId, UpdateInvoiceItemRequestDto dto);
    Boolean deleteItem(Long facturaId, Long itemId);
    PageImpl<InvoiceItemTableDto> listItems(Long facturaId, PageableDto<?> request);
}
