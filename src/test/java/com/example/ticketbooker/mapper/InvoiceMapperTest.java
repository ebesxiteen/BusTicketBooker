package com.example.ticketbooker.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.ticketbooker.DTO.Invoice.AddInvoiceDTO;
import com.example.ticketbooker.DTO.Invoice.ResponseInvoiceDTO;
import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Util.Enum.PaymentMethod;
import com.example.ticketbooker.Util.Enum.PaymentStatus;
import com.example.ticketbooker.Util.Mapper.InvoiceMapper;

class InvoiceMapperTest {

    @Test
    void fromAddCreatesInvoiceEntity() {
        AddInvoiceDTO dto = AddInvoiceDTO.builder()
                .totalAmount(500000)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentTime(LocalDateTime.of(2024, 10, 1, 10, 30))
                .paymentMethod(PaymentMethod.CASH)
                .build();

        Invoices invoice = InvoiceMapper.fromAdd(dto);

        assertEquals(500000, invoice.getTotalAmount());
        assertEquals(PaymentStatus.PENDING, invoice.getPaymentStatus());
        assertEquals(LocalDateTime.of(2024, 10, 1, 10, 30), invoice.getPaymentTime());
        assertEquals(PaymentMethod.CASH, invoice.getPaymentMethod());
    }

    @Test
    void toResponseDTOHandlesPageMetadata() {
        List<Invoices> content = new ArrayList<>();
        content.add(new Invoices(1, 100000, PaymentStatus.PAID, LocalDateTime.now(), PaymentMethod.VNPAY));
        content.add(new Invoices(2, 200000, PaymentStatus.PENDING, null, PaymentMethod.CASH));

        Page<Invoices> page = new PageImpl<>(content, PageRequest.of(1, 2), 10);

        ResponseInvoiceDTO response = InvoiceMapper.toResponseDTO(page);

        assertEquals(10, response.getInvoicesCount());
        assertEquals(2, response.getListInvoices().size());
        assertEquals(2, response.getPageSize());
        assertEquals(1, response.getCurrentPage());
        assertEquals(5, response.getTotalPages());
    }
}
