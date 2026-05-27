package com.example.ticketbooker.DTO.Invoice;

import java.util.ArrayList;

import com.example.ticketbooker.Entity.Invoices;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class ResponseInvoiceDTO {
    private int invoicesCount;
    private ArrayList<Invoices> listInvoices;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public ResponseInvoiceDTO() {
        this.invoicesCount = 0;
        this.listInvoices = new ArrayList<>();
        this.totalPages = 0;
        this.currentPage = 0;
        this.pageSize = 0;
    }

    public ResponseInvoiceDTO(int invoicesCount, ArrayList<Invoices> listInvoices) {
        this.invoicesCount = invoicesCount;
        this.listInvoices = listInvoices;
        this.totalPages = 0;
        this.currentPage = 0;
        this.pageSize = 0;
    }
    
    public ResponseInvoiceDTO(int invoicesCount, ArrayList<Invoices> listInvoices, int totalPages, int currentPage, int pageSize) {
        this.invoicesCount = invoicesCount;
        this.listInvoices = listInvoices;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }
}
