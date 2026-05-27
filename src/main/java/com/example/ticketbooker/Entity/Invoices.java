package com.example.ticketbooker.Entity;

import java.time.LocalDateTime;

import com.example.ticketbooker.Util.Enum.PaymentMethod;
import com.example.ticketbooker.Util.Enum.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@Table(name = "invoices")
public class Invoices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoiceId", nullable = false)
    private Integer id;

    @Column(name = "totalAmount")
    private Integer totalAmount;

    @Column(name = "paymentStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "paymentTime")
    private LocalDateTime paymentTime;

    @Column(name = "paymentMethod", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    public Invoices() {
        this.id = null;
        this.totalAmount = 0;
        this.paymentStatus = PaymentStatus.PENDING;
        this.paymentTime = null;
        this.paymentMethod = PaymentMethod.CASH;
    }

    public Invoices(Integer id, Integer totalAmount, PaymentStatus paymentStatus, LocalDateTime paymentTime, PaymentMethod paymentMethod) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.paymentTime = paymentTime;
        this.paymentMethod = paymentMethod;
    }
}