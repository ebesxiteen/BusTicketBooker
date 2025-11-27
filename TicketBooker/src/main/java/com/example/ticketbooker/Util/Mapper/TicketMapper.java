package com.example.ticketbooker.Util.Mapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.ticketbooker.DTO.Ticket.AddTicketRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforResponse;
import com.example.ticketbooker.DTO.Ticket.TicketDTO;
import com.example.ticketbooker.DTO.Ticket.TicketResponse;
import com.example.ticketbooker.DTO.Ticket.UpdateTicketRequest;
import com.example.ticketbooker.Entity.Tickets;
import com.example.ticketbooker.Entity.Users;

@Component
public class TicketMapper {

    // 1. TỪ ADD REQUEST -> ENTITY
    public static Tickets fromAdd(AddTicketRequest dto) {
        return Tickets.builder()
                .trip(dto.getTrip())
                .booker(dto.getBooker()) // Cả 2 đều là Users -> OK
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .seat(dto.getSeat())
                .ticketStatus(dto.getTicketStatus())
                .invoice(dto.getInvoices())
                .build();
    }

    // 2. TỪ UPDATE REQUEST -> ENTITY
    public static Tickets fromUpdate(UpdateTicketRequest dto) {
        return Tickets.builder()
                .id(dto.getId())
                .trip(dto.getTrip())
                .booker(dto.getBooker())
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .seat(dto.getSeat())
                .qrCode(dto.getQrCode())
                .ticketStatus(dto.getTicketStatus())
                .build();
    }

    // 3. TỪ ENTITY -> DTO (MỚI THÊM - QUAN TRỌNG)
    public static TicketDTO toDTO(Tickets entity) {
        if (entity == null) return null;
        
        return TicketDTO.builder()
                .id(entity.getId())
                .trip(entity.getTrip())
                .seat(entity.getSeat())
                .invoice(entity.getInvoice())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .qrCode(entity.getQrCode())
                .ticketStatus(entity.getTicketStatus())
                
                // Convert User Entity -> UserDTO để bảo mật (giấu pass)
                .booker(UserMapper.toDTO(entity.getBooker())) 
                .build();
    }

    // 4. ENTITY -> UPDATE DTO (Để đổ dữ liệu ra form sửa)
    public static UpdateTicketRequest toUpdateDTO(Tickets entity) {
        return UpdateTicketRequest.builder()
                .id(entity.getId())
                .trip(entity.getTrip())
                .booker(entity.getBooker())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .seat(entity.getSeat())
                .qrCode(entity.getQrCode())
                .ticketStatus(entity.getTicketStatus())
                .build();
    }

    // 5. LIST ENTITY -> RESPONSE DTO (ĐÃ SỬA LỖI LIST)
    public static TicketResponse toResponseDTO(List<Tickets> listEntities) {
        if (listEntities == null) return new TicketResponse();

        // Convert List<Tickets> -> List<TicketDTO>
        List<TicketDTO> dtos = listEntities.stream()
                .map(TicketMapper::toDTO)
                .collect(Collectors.toList());

        return TicketResponse.builder()
                .ticketsCount(dtos.size())
                .listTickets(dtos) // Bây giờ đã khớp kiểu List<TicketDTO>
                .build();
    }

    // 6. MAPPER CHO THANH TOÁN (PAYMENT INFO)
    public static PaymentInforResponse toPaymentInfor(Tickets entity) {
        return PaymentInforResponse.builder()
                .id(entity.getId())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .paymentTime(entity.getInvoice() != null ? LocalDate.from(entity.getInvoice().getPaymentTime()) : null)
                
                // Null check cho Booker để tránh lỗi NullPointerException
                .email(entity.getBooker() != null ? entity.getBooker().getEmail() : "")
                
                .totalAmount(entity.getInvoice() != null ? entity.getInvoice().getTotalAmount() : 0)
                .estimatedTime(LocalTime.from(entity.getTrip().getRoute().getEstimatedTime()))
                .departureLocation(entity.getTrip().getRoute().getDepartureLocation())
                .arrivalLocation(entity.getTrip().getRoute().getArrivalLocation())
                .departureTime(LocalDate.from(entity.getTrip().getDepartureTime()))
                .arrivalTime(entity.getTrip().getArrivalTime() != null ? LocalDate.from(entity.getTrip().getArrivalTime()) : null)
                .build();
    }

    // ========================================================================
    // 7. TỪ TICKET DTO -> UPDATE REQUEST (QUAN TRỌNG CHO CONTROLLER)
    // ========================================================================
    public static UpdateTicketRequest toUpdateDTO(TicketDTO dto) {
        if (dto == null) return null;

        // Tạo đối tượng Users giả lập từ UserDTO để gán vào Request
        // (Vì UpdateTicketRequest đang yêu cầu Users entity)
        Users bookerEntity = null;
        if (dto.getBooker() != null) {
            bookerEntity = new Users();
            bookerEntity.setId(dto.getBooker().getUserId());
            bookerEntity.setFullName(dto.getBooker().getFullName());
            bookerEntity.setEmail(dto.getBooker().getEmail());
            bookerEntity.setPhone(dto.getBooker().getPhone());
        }

        return UpdateTicketRequest.builder()
                .id(dto.getId())
                .trip(dto.getTrip())
                .booker(bookerEntity) 
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .seat(dto.getSeat())
                .qrCode(dto.getQrCode())
                .ticketStatus(dto.getTicketStatus())
                .build();
    }
}