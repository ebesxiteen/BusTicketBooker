package com.example.ticketbooker.Controller;

import java.net.URLDecoder;
import java.net.URLEncoder; // Thêm import này
import java.nio.charset.StandardCharsets; // Thêm import này
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ticketbooker.DTO.Invoice.AddInvoiceDTO;
import com.example.ticketbooker.DTO.Ticket.AddTicketRequest;
import com.example.ticketbooker.DTO.Trips.ResponseTripDTO;
import com.example.ticketbooker.DTO.Trips.SearchTripRequest;
import com.example.ticketbooker.Entity.Invoices;
import com.example.ticketbooker.Entity.Seats;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Service.InvoiceService;
import com.example.ticketbooker.Service.SeatsService;
import com.example.ticketbooker.Service.TicketService;
import com.example.ticketbooker.Service.TripService;
import com.example.ticketbooker.Service.UserService;
import com.example.ticketbooker.Util.SecurityUtils;
import com.example.ticketbooker.Util.Enum.PaymentMethod;
import com.example.ticketbooker.Util.Enum.PaymentStatus;
import com.example.ticketbooker.Util.Enum.TicketStatus;
import com.example.ticketbooker.Util.Utils.CookieUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/greenbus")
public class MainController {

    @Autowired
    private TripService tripService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private SeatsService seatsService;
    
    @Autowired
    private UserService userService;

    // 1. TRANG CHỦ
    @GetMapping()
    public String showMainPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = SecurityUtils.isLoggedIn();
        
        if (isLoggedIn) {
            Object principal = authentication.getPrincipal();
            Users user = SecurityUtils.extractUser(principal);
            
            if (user != null) {
                model.addAttribute("fullname", user.getFullName());
                boolean isAdmin = "ADMIN".equals(user.getRole()) || "MANAGER".equals(user.getRole());
                model.addAttribute("isAdmin", isAdmin);
            }
        }
        
        if (!model.containsAttribute("searchData")) {
            model.addAttribute("searchData", new SearchTripRequest());
        }
        
        model.addAttribute("isLoggedIn", isLoggedIn);
        return "View/User/Basic/TrangChu";
    }

    @GetMapping("/about-us")
    public String showAboutUs() { return "View/User/Basic/AboutUs"; }

    @GetMapping("/contact-us")
    public String showContactUs() { return "View/User/Basic/ContactUs"; }

    // 2. TRANG TÌM CHUYẾN
    @GetMapping("/find-trip")
    public String showtrip(@ModelAttribute("responseTripDTO") ResponseTripDTO responseTripDTO, Model model) {
        if (!model.containsAttribute("responseTripDTO")) {
            model.addAttribute("responseTripDTO", new ResponseTripDTO());
        }
        if (!model.containsAttribute("searchData")) {
            model.addAttribute("searchData", new SearchTripRequest());
        }
        
        LocalDateTime currentDate = LocalDateTime.now();
        model.addAttribute("currentDate", currentDate);
        return "View/User/Basic/FindTrip";
    }

    // XỬ LÝ CLICK TỪ TRANG CHỦ & URL
    @GetMapping("/search-trips")
    public String searchTripsFromLink(
            @RequestParam(value = "arrival", required = false) String arrival,
            @RequestParam(value = "departure", required = false) String departure,
            @RequestParam(value = "date", required = false) String dateStr,
            Model model) {

        SearchTripRequest searchRequest = new SearchTripRequest();
        searchRequest.setArrival(arrival);
        searchRequest.setDeparture(departure);

        if (dateStr == null || dateStr.isEmpty()) {
            searchRequest.setDepartureDate(LocalDateTime.now());
        } else {
            try {
                LocalDate d = LocalDate.parse(dateStr);
                searchRequest.setDepartureDate(d.atStartOfDay());
            } catch (Exception e) {
                searchRequest.setDepartureDate(LocalDateTime.now());
            }
        }

        ResponseTripDTO responseTripDTO = new ResponseTripDTO();
        if (arrival != null && !arrival.isEmpty()) {
            responseTripDTO = tripService.searchTrip(searchRequest);
        } else {
            responseTripDTO.setTripsCount(0);
        }

        model.addAttribute("responseTripDTO", responseTripDTO);
        model.addAttribute("searchData", searchRequest);
        model.addAttribute("currentDate", LocalDateTime.now());

        return "View/User/Basic/FindTrip";
    }

    @PostMapping("/search-trips")
    public String searchTrips(@ModelAttribute("searchData") SearchTripRequest searchTripRequest, RedirectAttributes redirectAttributes) {
        ResponseTripDTO responseTripDTO = tripService.searchTrip(searchTripRequest);
        redirectAttributes.addFlashAttribute("responseTripDTO", responseTripDTO);
        redirectAttributes.addFlashAttribute("searchData", searchTripRequest);
        return "redirect:/greenbus/find-trip";
    }

    // 3. TRANG ĐẶT VÉ (BOOKING)
    @GetMapping("/booking")
    public String showBooking(@RequestParam int tripId, Model model, HttpServletResponse response) {
        
        // --- SỬA ĐOẠN NÀY: Gửi kèm địa chỉ quay về ---
        if (!SecurityUtils.isLoggedIn()) {
            // Tạo link để quay lại trang booking này
            String returnUrl = "/greenbus/booking?tripId=" + tripId;
            // Encode để tránh lỗi ký tự đặc biệt
            String encodedUrl = URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
            
            return "redirect:/greenbus/login?redirect=" + encodedUrl; 
        }
        // --------------------------------------------

        model.addAttribute("bookingInformation", tripService.getTripById(Integer.valueOf(tripId)));
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = SecurityUtils.extractUser(authentication.getPrincipal());
        
        if (user != null) {
            Integer userId = user.getId();
            model.addAttribute("accountId", userId);
            model.addAttribute("fullname", user.getFullName());
            model.addAttribute("phone", user.getPhone());
            model.addAttribute("email", user.getEmail());
            CookieUtils.addCookie(response, "bookerId", Integer.toString(userId), "/", -1);
        }
        
        return "View/User/Basic/Booking";
    }

    @GetMapping("/paying")
    public String showPaying() { return "View/User/Basic/Paying"; }

    @GetMapping("/ticket-lookup")
    public String showTicketLookup() { 
        if (!SecurityUtils.isLoggedIn()) {
            // Tạo link để quay lại trang booking này
            String returnUrl = "/greenbus/ticket-lookup";
            // Encode để tránh lỗi ký tự đặc biệt
            String encodedUrl = URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
            
            return "redirect:/greenbus/login?redirect=" + encodedUrl; 
        }
        return "View/User/Basic/LookUpTicket"; 
    }

    // 4. TRANG CẢM ƠN
   // Trong MainController.java

@GetMapping("/thankyou")
public String showPaymentSuccess(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Model model,
                                 @RequestParam Map<String, String> allParams) {
    try {
        // Lấy các giá trị Cookie cơ bản
        int tripId = Integer.parseInt(CookieUtils.getCookieValue(request, "tripId", "0"));
        int bookerId = Integer.parseInt(CookieUtils.getCookieValue(request, "bookerId", "0"));
        String seatIdsRaw = CookieUtils.getCookieValue(request, "seatIds", "");
        
        // --- FIX: Kiểm tra dữ liệu quan trọng ---
        if (tripId == 0 || bookerId == 0 || seatIdsRaw.isEmpty()) {
            System.out.println("Lỗi: Thiếu thông tin trong Cookie (tripId, bookerId hoặc seatIds)");
            return "redirect:/greenbus"; // Hoặc trang lỗi
        }
        // ----------------------------------------

        String customerName = CookieUtils.getCookieValue(request, "customerName", "Khách lẻ");
        String customerPhone = CookieUtils.getCookieValue(request, "customerPhone", "");
        String grandTotalStr = CookieUtils.getCookieValue(request, "grandTotal", "0");
        int grandTotal = Integer.parseInt(grandTotalStr); // Parse ở đây cho gọn

        // Giải mã Seat IDs
        String seatIdsDecoded = URLDecoder.decode(seatIdsRaw, StandardCharsets.UTF_8);
        String[] listSeatIds = seatIdsDecoded.trim().split("\\s+");

        // Lấy trạng thái thanh toán từ URL (ZaloPay redirect về kèm param)
        // Lưu ý: ZaloPay trả về 'status' hoặc 'returncode', nhưng ở Service bạn đang set hardcode '?paymentStatus=1'
        int paymentStatus = 0;
        if (allParams.containsKey("paymentStatus")) {
             paymentStatus = Integer.parseInt(allParams.get("paymentStatus"));
        } else if (allParams.containsKey("status")) { 
             // Dự phòng trường hợp cổng thanh toán khác trả về 'status'
             paymentStatus = Integer.parseInt(allParams.get("status")); 
        }

       if (paymentStatus == 1) { // Thanh toán thành công (hoặc đặt đơn thành công với COD)

    // --- LOGIC MỚI BẮT ĐẦU ---
    
    // 1. Xác định phương thức và trạng thái thanh toán
    String methodParam = allParams.getOrDefault("paymentMethod", "");
    
    PaymentStatus finalStatus;
    PaymentMethod finalMethod;

    // Nếu trên URL có gửi paymentMethod=CASH (từ Booking.js)
    if ("CASH".equalsIgnoreCase(methodParam)) {
        finalStatus = PaymentStatus.PENDING; // COD thì chưa trả tiền -> PENDING
        finalMethod = PaymentMethod.CASH; 
    } else {
        // Các trường hợp khác (ZaloPay, VNPay...) mặc định là đã trả tiền
        finalStatus = PaymentStatus.PAID;
        finalMethod = PaymentMethod.EWALLET; 
    }

    // 2. Tạo Invoice với trạng thái động
    AddInvoiceDTO addInvoiceDTO = new AddInvoiceDTO(
            grandTotal,
            finalStatus,  // Dùng biến vừa logic ở trên
            LocalDateTime.now(),
            finalMethod   // Dùng biến vừa logic ở trên
    );
    // --- LOGIC MỚI KẾT THÚC ---

    int invoiceId = invoiceService.addInvoice(addInvoiceDTO);
    Invoices invoice = invoiceService.getById(invoiceId);

    // ... (Phần code dưới giữ nguyên) ...

            // 2. Tạo Request Add Ticket
            AddTicketRequest addRequest = AddTicketRequest.builder()
                    .customerName(customerName)
                    .customerPhone(customerPhone)
                    .tripId(tripId)
                    .bookerId(bookerId)
                    .ticketStatus(TicketStatus.BOOKED)
                    .invoices(invoice)
                    .seat(new ArrayList<>()) 
                    .build();

            // 3. Add ghế vào request
            for (String s : listSeatIds) {
                if (!s.isEmpty()) {
                    try {
                        addRequest.getSeat().add(Integer.parseInt(s));
                    } catch (NumberFormatException nfe) {
                        // Nếu cookie chứa seatCode (ví dụ A02) thay vì seatId, map sang seatId theo tripId
                        Seats seat = seatsService.getSeatByTripIdAndSeatCode(tripId, s);
                        if (seat != null) {
                            addRequest.getSeat().add(seat.getId());
                        } else {
                            System.out.println("Không tìm thấy seat cho mã: " + s);
                        }
                    }
                }
            }

            // 4. Lưu Ticket (Lúc này bảng ticket_seats mới được tạo)
            ticketService.addTicket(addRequest);

            // Xóa Cookie để tránh đặt lại (Optional)
            CookieUtils.addCookie(response, "seatIds", "", "/", 0);
            
        } else {
            // Thanh toán thất bại hoặc Hủy -> Xóa ghế đã giữ
            System.out.println("Thanh toán thất bại. Đang xóa ghế tạm...");
            for (String s : listSeatIds) {
                if (s.isEmpty()) continue;

                try {
                    seatsService.deleteSeat(Integer.parseInt(s));
                } catch (NumberFormatException nfe) {
                    Seats seat = seatsService.getSeatByTripIdAndSeatCode(tripId, s);
                    if (seat != null) {
                        seatsService.deleteSeat(seat.getId());
                    } else {
                        System.out.println("Không thể xóa seat, mã không hợp lệ: " + s);
                    }
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        return "redirect:/greenbus"; // Redirect về trang chủ nếu lỗi hệ thống
    }

    return "View/User/Basic/Thankyou";
}

    @GetMapping("/login")
    public String login() { return "View/Util/Login"; }

    @GetMapping("/profile")
    public String showProfile() { return "View/User/Registered/Profile/TicketHistory"; }

    @GetMapping("/reset-password")
    public String resetPassword() { return "View/Util/ResetPassword"; }
}