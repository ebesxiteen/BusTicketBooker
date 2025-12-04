package com.example.ticketbooker.Service.ServiceImp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.example.ticketbooker.DTO.Invoice.AddInvoiceDTO;
import com.example.ticketbooker.DTO.Invoice.RequestInvoiceDTO;
import com.example.ticketbooker.DTO.Invoice.ResponseInvoiceDTO;
import com.example.ticketbooker.DTO.Invoice.RevenueStatsDTO;
import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Repository.InvoiceRepo;
import com.example.ticketbooker.Service.InvoiceService;
import com.example.ticketbooker.Util.Enum.PaymentStatus;
import com.example.ticketbooker.Util.Mapper.InvoiceMapper;

@Service
public class InvoiceServiceImp implements InvoiceService {
    @Autowired
    private InvoiceRepo invoicesRepo;
    
    @Override
    public int addInvoice(AddInvoiceDTO dto) {
        try {
                        // 1. Lấy trip, giá vé 1 ghế


// 3. Tạo 1 invoice duy nhất
Invoices invoice = new Invoices();
invoice.setPaymentMethod(dto.getPaymentMethod());
invoice.setPaymentStatus(dto.getPaymentStatus());
invoice.setPaymentTime(LocalDateTime.now());
invoice.setTotalAmount(dto.getTotalAmount());
Invoices savedInvoice=invoicesRepo.save(invoice);

            return savedInvoice.getId();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }
    @Override
    public ResponseInvoiceDTO getAllInvoices() {
        ResponseInvoiceDTO result = new ResponseInvoiceDTO();
        try {
            result = InvoiceMapper.toResponseDTO(this.invoicesRepo.findAll());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return result;
        }
        return result;
    }
    public ResponseInvoiceDTO searchInvoices(RequestInvoiceDTO requestDTO) {
        ResponseInvoiceDTO result = new ResponseInvoiceDTO();
        try {
            int page = requestDTO.getPage() != null && requestDTO.getPage() >= 0 ? requestDTO.getPage() : 0;
            int size = requestDTO.getSize() != null && requestDTO.getSize() > 0 ? requestDTO.getSize() : 10;

            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentTime"));

            Page<Invoices> filteredInvoices = invoicesRepo.searchInvoices(
                    requestDTO.getTotalAmount(),
                    requestDTO.getPaymentStatus(),
                    requestDTO.getPaymentMethod(),
                    pageRequest
            );
            result = InvoiceMapper.toResponseDTO(filteredInvoices);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    @Override
    public Invoices getById(int id) {
        return this.invoicesRepo.findById(id).orElse(null);
    }

    @Override
    public RevenueStatsDTO getRevenueStats(String period, LocalDate selectedDate) {
        LocalDate previousDate;
        switch (period) {
            case "Day":
                previousDate = selectedDate.minusDays(1);
                break;
            case "Month":
                previousDate = selectedDate.minusMonths(1);
                break;
            case "Year":
                previousDate = selectedDate.minusYears(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        double currentPeriodRevenue = calculateRevenue(period, selectedDate);
        double previousPeriodRevenue = calculateRevenue(period, previousDate);


        return RevenueStatsDTO.builder()
                .period(period)
                .selectedDate(selectedDate)
                .currentPeriodRevenue(currentPeriodRevenue)
                .previousPeriodRevenue(previousPeriodRevenue)
                .build();
    }


    private double calculateRevenue(String period, LocalDate date) {
        LocalDateTime start, end;
        switch (period) {
            case "Day":
                start = date.atStartOfDay();
                end = date.plusDays(1).atStartOfDay();
                break;
            case "Month":
                start = date.withDayOfMonth(1).atStartOfDay();
                end = date.plusMonths(1).withDayOfMonth(1).atStartOfDay();
                break;
            case "Year":
                start = date.withDayOfYear(1).atStartOfDay();
                end = date.plusYears(1).withDayOfYear(1).atStartOfDay();
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        List<Invoices> invoices = invoicesRepo.findAllByPaymentTimeBetweenAndPaymentStatus(start, end, PaymentStatus.PAID); // Ensure you have PaymentStatus.PAID
        return invoices.stream().mapToDouble(Invoices::getTotalAmount).sum();

    }

    @Override
    public boolean updatePaymentStatus(int id, PaymentStatus paymentStatus) {
        if (paymentStatus == PaymentStatus.CANCELLED) {
            return false;
        }

        Invoices invoice = invoicesRepo.findById(id).orElse(null);
        if (invoice == null || invoice.getPaymentStatus() == PaymentStatus.CANCELLED) {
            return false;
        }

        invoice.setPaymentStatus(paymentStatus);
        invoicesRepo.save(invoice);
        return true;
    }
}
