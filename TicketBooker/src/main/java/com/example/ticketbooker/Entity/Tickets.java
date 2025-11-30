package com.example.ticketbooker.Entity;

import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

// SỬA LẠI ĐOẠN NÀY
@ManyToMany(fetch = FetchType.EAGER) // Nên để EAGER nếu bạn muốn load ghế luôn mà không bị lỗi Lazy
@JoinTable(
        name = "ticket_seats",
        joinColumns = @JoinColumn(name = "ticketId"),      // Đã sửa ticket_id -> ticketId
        inverseJoinColumns = @JoinColumn(name = "seatId")  // Đã sửa seat_id -> seatId
)   
    @Builder.Default
    private List<Seats> seats = new ArrayList<>();

    @Column(name = "qrCode")
    private String qrCode;

    @Column(name = "ticketStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;
}