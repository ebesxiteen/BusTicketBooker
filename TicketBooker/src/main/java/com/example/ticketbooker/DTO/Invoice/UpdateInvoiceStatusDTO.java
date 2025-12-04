package com.example.ticketbooker.DTO.Invoice;

import com.example.ticketbooker.Util.Enum.PaymentStatus;
import lombok.Data;

@Data
public class UpdateInvoiceStatusDTO {
    private PaymentStatus paymentStatus;
}

