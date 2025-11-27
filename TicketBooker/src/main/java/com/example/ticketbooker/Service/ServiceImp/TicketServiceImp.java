package com.example.ticketbooker.Service.ServiceImp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.ticketbooker.DTO.Ticket.AddTicketRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforResponse;
import com.example.ticketbooker.DTO.Ticket.TicketIdRequest;
import com.example.ticketbooker.DTO.Ticket.TicketResponse;
import com.example.ticketbooker.DTO.Ticket.TicketStatsDTO;
import com.example.ticketbooker.DTO.Ticket.UpdateTicketRequest;
import com.example.ticketbooker.Entity.Tickets;
import com.example.ticketbooker.Repository.TicketRepo;
import com.example.ticketbooker.Service.TicketService;
import com.example.ticketbooker.Util.Enum.TicketStatus;
import com.example.ticketbooker.Util.Mapper.TicketMapper;

@Service
public class TicketServiceImp implements TicketService {
    @Autowired
    private TicketRepo ticketRepository;

    @Override
    public boolean addTicket(AddTicketRequest dto) {
        try {
            Tickets ticket = TicketMapper.fromAdd(dto);
            ticketRepository.save(ticket);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateTicket(UpdateTicketRequest dto) {
        try {
            Tickets ticket = TicketMapper.fromUpdate(dto);
            ticketRepository.save(ticket);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteTicket(TicketIdRequest dto) {
        try {
            ticketRepository.deleteById(dto.getId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public TicketResponse getAllTickets() {
        try {
            // toResponseDTO đã được viết lại để nhận List<Entity> và trả về Response chứa DTO
            return TicketMapper.toResponseDTO(this.ticketRepository.findAll());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new TicketResponse();
        }
    }

    @Override
    public TicketResponse getTicketById(TicketIdRequest dto) {
        try {
            // findAllById trả về List, Mapper sẽ xử lý
            return TicketMapper.toResponseDTO(this.ticketRepository.findAllById(Collections.singletonList(dto.getId())));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new TicketResponse();
        }
    }

    @Override
    public PaymentInforResponse getPaymentInfo(PaymentInforRequest request) {
        try {
            Optional<Tickets> ticketOptional = this.ticketRepository.findById(request.getTicketId());
            if (ticketOptional.isPresent()) {
                Tickets ticket = ticketOptional.get();

                if (ticket.getTicketStatus() == TicketStatus.BOOKED) {
                    if (ticket.getCustomerPhone().equals(request.getCustomerPhone())) {
                        return TicketMapper.toPaymentInfor(ticket);
                    } else {
                        System.out.println("Số điện thoại không trùng khớp.");
                    }
                } else {
                    System.out.println("Trạng thái ticket không hợp lệ: " + ticket.getTicketStatus());
                }
            } else {
                System.out.println("Không tìm thấy ticket với ID: " + request.getTicketId());
            }
        } catch (Exception e) {
            System.out.println("Lỗi trong quá trình xử lý: " + e.getMessage());
        }
        return null;
    }

    @Override
    public TicketResponse getTicketsByUserId(int userId) { // Đổi tên hàm và tham số
        try {
            // Nhớ sửa cả tên biến truyền vào repo
            List<Tickets> tickets = ticketRepository.findAllByBookerId(userId); 
            return TicketMapper.toResponseDTO(tickets);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new TicketResponse();
        }
    }

    @Override
    public TicketResponse searchTickets(int accountId, Integer ticketId, LocalDate departureDate, String route, TicketStatus status) {
        try {
            List<Tickets> tickets = ticketRepository.searchTickets(accountId, ticketId, departureDate, route, status);
            return TicketMapper.toResponseDTO(tickets);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new TicketResponse();
        }
    }

    @Override
    public TicketStatsDTO getTicketStats(String period, LocalDate selectedDate) {
        LocalDate previousDate = getPreviousDate(period, selectedDate);
        int currentPeriodCount = getTicketCountByPeriod(period, selectedDate);
        int previousPeriodCount = getTicketCountByPeriod(period, previousDate);

        return TicketStatsDTO.builder()
                .period(period)
                .selectedDate(selectedDate)
                .currentPeriodTicketCount(currentPeriodCount)
                .previousPeriodTicketCount(previousPeriodCount)
                .build();
    }

    private int getTicketCountByPeriod(String period, LocalDate date) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        switch (period) {
            case "Day":
                start = date.atStartOfDay();
                end = date.plusDays(1).atStartOfDay();
                break;
            case "Month":
                start = date.withDayOfMonth(1).atStartOfDay();
                end = date.plusMonths(1).withDayOfMonth(1).atStartOfDay();
                break;
            case "Year":
                start = date.withDayOfYear(1).atStartOfDay();
                end = date.plusYears(1).withDayOfYear(1).atStartOfDay();
                break;
        }
        return ticketRepository.countTicketsByPaymentTimeBetween(start, end);
    }

    private LocalDate getPreviousDate(String period, LocalDate date) {
        switch (period) {
            case "Day": return date.minusDays(1);
            case "Month": return date.minusMonths(1);
            case "Year": return date.minusYears(1);
            default: return date;
        }
    }

    @Override
    public TicketResponse getAllTickets(Pageable pageable) {
        Page<Tickets> ticketPage = ticketRepository.findAll(pageable);
        // Sử dụng Constructor mới của TicketResponse (nhận Page<Tickets>)
        return new TicketResponse(ticketPage);
    }

    @Override
    public TicketResponse getTicketsByTripId(int tripId, Pageable pageable) {
        Page<Tickets> ticketPage = ticketRepository.findAllByTripId(tripId, pageable);
        return new TicketResponse(ticketPage);
    }

    // --- GIỮ NGUYÊN CÁC HÀM EXCEL (KHÔNG ẢNH HƯỞNG) ---
    @Override
    public ByteArrayInputStream exportTicketsToExcelByTripId(int tripId) {
        List<Tickets> tickets = ticketRepository.findAllByTripId(tripId);
        return generateExcel(tickets);
    }

    @Override
    public ByteArrayInputStream exportAllTicketsToExcel() {
        List<Tickets> tickets = ticketRepository.findAll();
        return generateExcel(tickets);
    }

    // Viết hàm chung để đỡ lặp code
    private ByteArrayInputStream generateExcel(List<Tickets> tickets) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tickets");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Ticket ID", "Customer Name", "Customer Phone", "Seat ID", "Ticket Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (Tickets ticket : tickets) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(ticket.getId());
                row.createCell(1).setCellValue(ticket.getCustomerName());
                row.createCell(2).setCellValue(ticket.getCustomerPhone());
                row.createCell(3).setCellValue(ticket.getSeat().getId());
                row.createCell(4).setCellValue(ticket.getTicketStatus().name());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}