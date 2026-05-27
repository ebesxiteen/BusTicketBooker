package com.example.ticketbooker.DTO.Ticket;

import java.util.List;

import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Util.Enum.TicketStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AddTicketRequest {
    @NotNull
    private Integer tripId;

    private Integer bookerId;

    @NotBlank
    private String customerName;

    @NotBlank
    private String customerPhone;

    @NotEmpty
    @Builder.Default
    private List<Integer> seat = new java.util.ArrayList<>();

    private TicketStatus ticketStatus;

    @NotNull
    private Invoices invoices;
}
