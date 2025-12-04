package com.example.ticketbooker.Util.Mapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.ticketbooker.DTO.Ticket.PaymentInforResponse;
import com.example.ticketbooker.DTO.Ticket.TicketDTO;
import com.example.ticketbooker.DTO.Ticket.TicketResponse;
import com.example.ticketbooker.DTO.Ticket.UpdateTicketRequest;
import com.example.ticketbooker.Entity.Seats;
import com.example.ticketbooker.Entity.Tickets;
import com.example.ticketbooker.Entity.Users;

@Component
public class TicketMapper {
    

    // 3. TỪ ENTITY -> DTO (MỚI THÊM - QUAN TRỌNG)
    public static TicketDTO toDTO(Tickets entity) {
        
    if (entity == null) return null;

    String seatCodes = "";
    if (entity.getSeats() != null && !entity.getSeats().isEmpty()) {
        seatCodes = entity.getSeats().stream()
                .map(Seats::getSeatCode)
                .collect(Collectors.joining(", "));
    }

    return TicketDTO.builder()
            .id(entity.getId())
            .trip(entity.getTrip())
            .invoice(entity.getInvoice())
            .customerName(entity.getCustomerName())
            .customerPhone(entity.getCustomerPhone())
            .qrCode(entity.getQrCode())
            .ticketStatus(entity.getTicketStatus())
            .seatCodes(seatCodes)              // field mới trong TicketDTO
            .booker(UserMapper.toDTO(entity.getBooker()))  // Chuyển Users entity sang UserDTO rồi lấy userId
            .build();
}


    // 4. ENTITY -> UPDATE DTO (Để đổ dữ liệu ra form sửa)
  public static UpdateTicketRequest toUpdateDTO(Tickets entity) {
    if (entity == null) return null;

    // map list seats -> list seatIds
    List<Integer> seatIds = new java.util.ArrayList<>();
    if (entity.getSeats() != null) {
        for (Seats s : entity.getSeats()) {
            seatIds.add(s.getId());
        }
    }

    return UpdateTicketRequest.builder()
            .id(entity.getId())
            .tripId(entity.getTrip() != null ? entity.getTrip().getId() : 0)
            .bookerId(entity.getBooker() != null ? entity.getBooker().getId() : 0)
            .invoice(entity.getInvoice())
            .customerName(entity.getCustomerName())
            .customerPhone(entity.getCustomerPhone())
            .seat(seatIds)
            .qrCode(entity.getQrCode())
            .ticketStatus(entity.getTicketStatus())
            .build();
}



    // 5. LIST ENTITY -> RESPONSE DTO (ĐÃ SỬA LỖI LIST)
    public static TicketResponse toResponseDTO(List<Tickets> listEntities) {
    if (listEntities == null) return new TicketResponse();

    List<TicketDTO> dtos = listEntities.stream()
            .map(TicketMapper::toDTO)   // dùng hàm ở trên
            .collect(Collectors.toList());

    return TicketResponse.builder()
            .ticketsCount(dtos.size())
            .listTickets(dtos)
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
        List<Integer> seatIds = new java.util.ArrayList<>();
        if (dto.getSeatCodes() != null && !dto.getSeatCodes().isBlank()) {
        String[] parts = dto.getSeatCodes().split(",");
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                seatIds.add(Integer.parseInt(part)); // nếu id ghế là số
            }
        }
    }

        return UpdateTicketRequest.builder()
                .id(dto.getId())
                .tripId(dto.getTrip().getId())
                .bookerId(bookerEntity.getId()) 
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .seat(seatIds)
                .qrCode(dto.getQrCode())
                .ticketStatus(dto.getTicketStatus())
                .build();
    }
}