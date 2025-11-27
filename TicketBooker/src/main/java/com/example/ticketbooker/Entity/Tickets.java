package com.example.ticketbooker.Entity;

import com.example.ticketbooker.Util.Enum.TicketStatus;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@Table(name = "tickets")
@NoArgsConstructor  // Lombok tự sinh Constructor rỗng
@AllArgsConstructor // Lombok tự sinh Constructor đầy đủ
public class Tickets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticketId", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tripId")
    private Trips trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookerId")
    @Nullable
    private Users booker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoiceId")
    private Invoices invoice;

    @Column(name = "customerName", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customerPhone", nullable = false, length = 15)
    private String customerPhone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seatId")
    private Seats seat;

    @Column(name = "qrCode")
    private String qrCode;

    @Column(name = "ticketStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;
}