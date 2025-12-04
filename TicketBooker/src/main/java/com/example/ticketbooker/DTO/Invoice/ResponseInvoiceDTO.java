package com.example.ticketbooker.DTO.Invoice;

import com.example.ticketbooker.Entity.Invoices;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
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
}
