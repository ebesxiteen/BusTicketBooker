package com.example.ticketbooker.DTO.Invoice;

import com.example.ticketbooker.Util.Enum.PaymentMethod;
import com.example.ticketbooker.Util.Enum.PaymentStatus;
import lombok.Data;

@Data
public class RequestInvoiceDTO {
    private Integer totalAmount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private Integer page;
    private Integer size;
    public RequestInvoiceDTO() {
        this.totalAmount = null;
        this.paymentStatus = null;
        this.paymentMethod = null;
        this.page = 0;
        this.size = 10;
    }
    public RequestInvoiceDTO(Integer totalAmount, PaymentStatus paymentStatus, PaymentMethod paymentMethod) {
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.page = 0;
        this.size = 10;
    }

}
