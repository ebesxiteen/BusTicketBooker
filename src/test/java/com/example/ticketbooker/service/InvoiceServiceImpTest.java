package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ticketbooker.DTO.Invoice.AddInvoiceDTO;
import com.example.ticketbooker.DTO.Invoice.RevenueStatsDTO;
import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Repository.InvoiceRepo;
import com.example.ticketbooker.Repository.TicketRepo;
import com.example.ticketbooker.Service.ServiceImp.InvoiceServiceImp;
import com.example.ticketbooker.Util.Enum.PaymentMethod;
import com.example.ticketbooker.Util.Enum.PaymentStatus;
import com.example.ticketbooker.Util.Enum.TicketStatus;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImpTest {

    @Mock
    private InvoiceRepo invoicesRepo;

    @Mock
    private TicketRepo ticketRepo;

    @InjectMocks
    private InvoiceServiceImp invoiceServiceImp;

    @Test
    void addInvoiceSavesInvoiceAndReturnsSavedId() {
        AddInvoiceDTO request = new AddInvoiceDTO(250000, PaymentStatus.PENDING, null, PaymentMethod.VNPAY);
        ArgumentCaptor<Invoices> invoiceCaptor = ArgumentCaptor.forClass(Invoices.class);
        when(invoicesRepo.save(any(Invoices.class))).thenAnswer(invocation -> {
            Invoices invoice = invocation.getArgument(0);
            invoice.setId(12);
            return invoice;
        });

        int id = invoiceServiceImp.addInvoice(request);

        assertEquals(12, id);
        verify(invoicesRepo).save(invoiceCaptor.capture());
        assertEquals(250000, invoiceCaptor.getValue().getTotalAmount());
        assertEquals(PaymentMethod.VNPAY, invoiceCaptor.getValue().getPaymentMethod());
    }

    @Test
    void updatePaymentStatusRejectsCancelledTargetStatus() {
        boolean result = invoiceServiceImp.updatePaymentStatus(12, PaymentStatus.CANCELLED);

        assertFalse(result);
        verify(invoicesRepo, never()).findById(any());
    }

    @Test
    void updatePaymentStatusRejectsDowngradeWhenUsedTicketExists() {
        Invoices invoice = new Invoices(12, 250000, PaymentStatus.PAID, LocalDateTime.now(), PaymentMethod.VNPAY);
        when(invoicesRepo.findById(12)).thenReturn(Optional.of(invoice));
        when(ticketRepo.existsByInvoice_IdAndTicketStatus(12, TicketStatus.USED)).thenReturn(true);

        boolean result = invoiceServiceImp.updatePaymentStatus(12, PaymentStatus.PENDING);

        assertFalse(result);
        verify(invoicesRepo, never()).save(any());
    }

    @Test
    void updatePaymentStatusSavesWhenStatusChanges() {
        Invoices invoice = new Invoices(12, 250000, PaymentStatus.PENDING, null, PaymentMethod.VNPAY);
        when(invoicesRepo.findById(12)).thenReturn(Optional.of(invoice));

        boolean result = invoiceServiceImp.updatePaymentStatus(12, PaymentStatus.PAID);

        assertTrue(result);
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
        verify(invoicesRepo).save(invoice);
    }

    @Test
    void getRevenueStatsSumsCurrentAndPreviousPeriods() {
        LocalDate selectedDate = LocalDate.of(2026, 5, 29);
        when(invoicesRepo.findAllByPaymentTimeBetweenAndPaymentStatus(any(), any(), eq(PaymentStatus.PAID)))
                .thenReturn(List.of(new Invoices(1, 100000, PaymentStatus.PAID, null, PaymentMethod.CASH)))
                .thenReturn(List.of(new Invoices(2, 50000, PaymentStatus.PAID, null, PaymentMethod.CASH)));

        RevenueStatsDTO result = invoiceServiceImp.getRevenueStats("Day", selectedDate);

        assertEquals("Day", result.getPeriod());
        assertEquals(100000.0, result.getCurrentPeriodRevenue());
        assertEquals(50000.0, result.getPreviousPeriodRevenue());
    }

    @Test
    void getRevenueStatsRejectsInvalidPeriod() {
        assertThrows(IllegalArgumentException.class, () -> invoiceServiceImp.getRevenueStats("Week", LocalDate.now()));
    }
}
