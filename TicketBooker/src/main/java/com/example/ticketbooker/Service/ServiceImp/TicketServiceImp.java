package com.example.ticketbooker.Service.ServiceImp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketbooker.DTO.Ticket.AddTicketRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforRequest;
import com.example.ticketbooker.DTO.Ticket.PaymentInforResponse;
import com.example.ticketbooker.DTO.Ticket.TicketIdRequest;
import com.example.ticketbooker.DTO.Ticket.TicketResponse;
import com.example.ticketbooker.DTO.Ticket.TicketStatsDTO;
import com.example.ticketbooker.DTO.Ticket.UpdateTicketRequest;
import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Entity.Seats;
import com.example.ticketbooker.Entity.Tickets;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Repository.InvoiceRepo;
import com.example.ticketbooker.Repository.SeatsRepo;
import com.example.ticketbooker.Repository.TicketRepo;
import com.example.ticketbooker.Repository.TripRepo;
import com.example.ticketbooker.Repository.UserRepo;
import com.example.ticketbooker.Service.TicketService;
import com.example.ticketbooker.Service.OutSource.EmailService;
import com.example.ticketbooker.Util.Enum.PaymentStatus;
import com.example.ticketbooker.Util.Enum.TicketStatus;
import com.example.ticketbooker.Util.Mapper.TicketMapper;

@Service
public class TicketServiceImp implements TicketService {
    @Autowired
    private TicketRepo ticketRepository;
    @Autowired
    private SeatsRepo seatsRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepo userRepository;  // repo của entity Users
    @Autowired
    private TripRepo tripRepos;
    @Autowired
    private InvoiceRepo invoiceRepo;


   @Override
public boolean addTicket(AddTicketRequest dto) {
    try {
        // 1. Lấy trip
        Trips trip = tripRepos.findById(dto.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found with id: " + dto.getTripId()));

        // 2. Lấy booker
        Users booker = null;
        if (dto.getBookerId() != null) {
            booker = userRepository.findById(dto.getBookerId()).orElse(null);
        }

        // 3. Lấy invoice (đã tạo ở /thankyou)
        Invoices invoice = dto.getInvoices();

        // 4. Lấy danh sách ghế
        List<Seats> seatEntities = new java.util.ArrayList<>();
        for (Integer seatId : dto.getSeat()) {
            Seats seat = seatsRepo.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found with id: " + seatId));
            seatEntities.add(seat);
        }

        // 5. Tạo 1 ticket chứa nhiều ghế
        Tickets ticket = Tickets.builder()
                .trip(trip)
                .booker(booker)
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .seats(seatEntities)          // ❗ dùng List<Seat>
                .ticketStatus(dto.getTicketStatus())
                .invoice(invoice)             // 1 invoice chung
                .build();

        ticketRepository.save(ticket);

// 6. Gửi email xác nhận đặt vé thành công
if (booker != null && booker.getEmail() != null) {

    // Format seatCodes
    String seatCodes = seatEntities.stream()
            .map(seat -> seat.getSeatCode())
            .collect(Collectors.joining(", "));

    // Format tổng tiền
    String formattedAmount = String.format("%,d", invoice.getTotalAmount());

    // Format thời gian khởi hành
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    String formattedDeparture = trip.getDepartureTime().format(formatter);

    // Lấy biển số xe
String licensePlate = trip.getBus() != null ? trip.getBus().getLicensePlate() : "Đang cập nhật";

// Email
String html = ""
        + "<html><body style='font-family:Arial, sans-serif; line-height:1.6;'>"
        + "<p>Xin chào <b>" + booker.getFullName() + "</b>,</p>"
        + "<p>Bạn đã <span style='color:green;font-weight:bold;'>ĐẶT VÉ THÀNH CÔNG</span> tại hệ thống GreenBus.</p>"
        + "<p>Thông tin vé của bạn:</p>"

        + "<ul>"
        + "<li><b>Mã vé:</b> " + ticket.getId() + "</li>"
        + "<li><b>Tuyến đường:</b> " 
            + trip.getRoute().getDepartureLocation() + " → " 
            + trip.getRoute().getArrivalLocation() + "</li>"
        + "<li><b>Thời gian khởi hành:</b> " + formattedDeparture + "</li>"
        + "<li><b>Biển số xe:</b> " + licensePlate + "</li>"
        + "<li><b>Số ghế:</b> " + seatCodes + "</li>"
        + "<li><b>Mã hóa đơn:</b> " + invoice.getId() + "</li>"
        + "<li><b>Tổng tiền:</b> " + formattedAmount + " VND</li>"
        + "</ul>"

        + "<p>Cảm ơn bạn đã lựa chọn <b>GreenBus</b>. Chúc bạn có một chuyến đi an toàn và thoải mái!</p>"
        + "<p>Trân trọng,<br/><b>GreenBus Line</b></p>"
        + "</body></html>";


    emailService.sendHtmlContent(
            booker.getEmail(),
            "Xác nhận đặt vé thành công - GreenBus",
            html
    );
}


        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}


    @Override
    @Transactional
    public boolean updateTicket(UpdateTicketRequest dto) {
        try {
            // 1. Lấy ticket hiện tại từ DB
            Tickets ticket = ticketRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + dto.getId()));

            TicketStatus currentStatus = ticket.getTicketStatus();
            boolean cancellingTicket = dto.getTicketStatus() == TicketStatus.CANCELLED;

            if (currentStatus == TicketStatus.USED && dto.getTicketStatus() != TicketStatus.USED) {
                return false;
            }

            if (currentStatus == TicketStatus.CANCELLED && dto.getTicketStatus() != TicketStatus.CANCELLED) {
                return false;
            }

            // 2. Cập nhật trip nếu tripId khác 0
            if (dto.getTripId() != 0) {
                Optional<Trips> trip = tripRepos.findById(dto.getTripId());
                if (trip == null) {
                    throw new RuntimeException("Trip not found with id: " + dto.getTripId());
                }
            }

            // 3. Cập nhật booker nếu bookerId khác 0
            if (dto.getBookerId() != 0) {
                Users booker = userRepository.findById(dto.getBookerId())
                        .orElseThrow(() -> new RuntimeException("User not found: " + dto.getBookerId()));
                ticket.setBooker(booker);
            }

            // 4. Cập nhật invoice (nếu cho phép sửa)
            if (!cancellingTicket && dto.getInvoice() != null) {
                ticket.setInvoice(dto.getInvoice());
            }

            // 5. Xử lý danh sách ghế
            if (cancellingTicket) {
                List<Seats> seatsToRemove = new java.util.ArrayList<>(ticket.getSeats());
                ticket.getSeats().clear(); // xóa liên kết bảng ticket_seats
                if (!seatsToRemove.isEmpty()) {
                    seatsRepo.deleteAll(seatsToRemove); // xóa ghế trong bảng seats
                }
            } else if (dto.getSeat() != null && !dto.getSeat().isEmpty()) {
                List<Seats> seatEntities = new java.util.ArrayList<>();
                for (Integer seatId : dto.getSeat()) {
                    Seats seat = seatsRepo.findById(seatId)
                            .orElseThrow(() -> new RuntimeException("Seat not found: " + seatId));
                    seatEntities.add(seat);
                }
                ticket.setSeats(seatEntities);   // nhớ: field trong Tickets là List<Seat> seats
            }

            // 6. Cập nhật các field đơn giản
            ticket.setCustomerName(dto.getCustomerName());
            ticket.setCustomerPhone(dto.getCustomerPhone());
            ticket.setQrCode(dto.getQrCode());
            ticket.setTicketStatus(dto.getTicketStatus());

            PaymentStatus previousPaymentStatus = null;
            if (cancellingTicket && ticket.getInvoice() != null) {
                previousPaymentStatus = ticket.getInvoice().getPaymentStatus();
                ticket.getInvoice().setPaymentStatus(PaymentStatus.CANCELLED);
                invoiceRepo.save(ticket.getInvoice());
                
                if (previousPaymentStatus == PaymentStatus.PAID) {
                    Users booker = ticket.getBooker();
                    if (booker != null && booker.getEmail() != null) {
                        String formattedAmount = String.format("%,d", ticket.getInvoice().getTotalAmount());
                        String html = "<html><body style='font-family:Arial, sans-serif; line-height:1.6;'>"
                                + "<p>Xin chào <b>" + booker.getFullName() + "</b>,</p>"
                                + "<p>Vé của bạn đã được hủy. Chúng tôi đã tiến hành hoàn tiền cho hóa đơn #"
                                + ticket.getInvoice().getId() + " với số tiền <b>" + formattedAmount + "đ</b>.</p>"
                                + "<p>Nếu có thắc mắc, vui lòng liên hệ tổng đài <b>1900 1990</b>.</p>"
                                + "<p>Trân trọng,<br/>GreenBus Line</p>"
                                + "</body></html>";

                        emailService.sendHtmlContent(
                                booker.getEmail(),
                                "Thông báo hoàn tiền - GreenBus",
                                html
                        );
                    }
                }
            }

            // 7. Lưu lại
            ticketRepository.save(ticket);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
        System.out.println("Vao ham getTicketById voi id: " + dto.getId());
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

      @Override
    public TicketResponse getTicketsByStatus(TicketStatus status, Pageable pageable) {
        Page<Tickets> ticketPage = ticketRepository.findAllByTicketStatus(status, pageable);
        return new TicketResponse(ticketPage);
    }

    @Override
    public TicketResponse getTicketsByTripIdAndStatus(int tripId, TicketStatus status, Pageable pageable) {
        Page<Tickets> ticketPage = ticketRepository.findAllByTicketStatusAndTripId(status, tripId, pageable);
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

           // Sửa lại đoạn loop trong generateExcel
int rowIdx = 1;
for (Tickets ticket : tickets) {
    Row row = sheet.createRow(rowIdx++);
    row.createCell(0).setCellValue(ticket.getId());
    row.createCell(1).setCellValue(ticket.getCustomerName());
    row.createCell(2).setCellValue(ticket.getCustomerPhone());

    // --- SỬA ĐOẠN NÀY ---
    String seatCodes = "";
    if (ticket.getSeats() != null) {
        seatCodes = ticket.getSeats().stream()
                .map(seat -> seat.getSeatCode()) // Lấy code của đúng ticket hiện tại
                .collect(Collectors.joining(", "));
    }
    row.createCell(3).setCellValue(seatCodes);
    // --------------------

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