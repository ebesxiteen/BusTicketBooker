package com.example.ticketbooker.Util.Mapper;

import com.example.ticketbooker.DTO.Invoice.AddInvoiceDTO;
import com.example.ticketbooker.DTO.Invoice.ResponseInvoiceDTO;
import com.example.ticketbooker.Entity.Invoices;
import org.springframework.data.domain.Page;

import java.util.ArrayList;

public class InvoiceMapper {
    public static Invoices fromAdd(AddInvoiceDTO dto) {
        return Invoices.builder()
                .totalAmount(dto.getTotalAmount())
                .paymentStatus(dto.getPaymentStatus())
                .paymentTime(dto.getPaymentTime())
                .paymentMethod(dto.getPaymentMethod())
                .build();
    }
    public static ResponseInvoiceDTO toResponseDTO(ArrayList<Invoices> invoices) {
        return ResponseInvoiceDTO.builder()
                .invoicesCount(invoices.size())
                .listInvoices(invoices)
                .totalPages(1)
                .currentPage(0)
                .pageSize(invoices.size())
                .build();
    }

    public static ResponseInvoiceDTO toResponseDTO(Page<Invoices> invoicesPage) {
        return ResponseInvoiceDTO.builder()
                .invoicesCount((int) invoicesPage.getTotalElements())
                .listInvoices(new ArrayList<>(invoicesPage.getContent()))
                .totalPages(invoicesPage.getTotalPages())
                .currentPage(invoicesPage.getNumber())
                .pageSize(invoicesPage.getSize())
                .build();
    }
}
