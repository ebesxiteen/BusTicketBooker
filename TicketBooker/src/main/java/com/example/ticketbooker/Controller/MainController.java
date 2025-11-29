package com.example.ticketbooker.Controller;

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
   @GetMapping("/thankyou")
public String showPaymentSuccess(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Model model,
                                 @RequestParam Map<String, String> allParams) {
    try {
        int tripId = Integer.parseInt(CookieUtils.getCookieValue(request, "tripId", "0"));
        String customerName = CookieUtils.getCookieValue(request, "customerName", "");
        int bookerId = Integer.parseInt(CookieUtils.getCookieValue(request, "bookerId", "0"));
        String grandTotal = CookieUtils.getCookieValue(request, "grandTotal", "0");
        String customerPhone = CookieUtils.getCookieValue(request, "customerPhone", "");
        String seatIdsStr = CookieUtils.getCookieValue(request, "seatIds", "");
        String[] listSeatIds = seatIdsStr.trim().split("\\s+");

        int paymentStatus = Integer.parseInt(allParams.getOrDefault("paymentStatus", "0"));

        if (paymentStatus == 1) {

            // bắt buộc có booker
            if (bookerId == 0) {
                return "redirect:/greenbus/login";
            }

            // 1️⃣ tạo 1 invoice duy nhất cho cả lượt đặt
            AddInvoiceDTO addInvoiceDTO = new AddInvoiceDTO(
                    Integer.parseInt(grandTotal),
                    PaymentStatus.PAID,
                    LocalDateTime.now(),
                    PaymentMethod.EWALLET
            );
            int invoiceId = invoiceService.addInvoice(addInvoiceDTO);
            Invoices invoice = invoiceService.getById(invoiceId);

            // 2️⃣ tạo AddTicketRequest: 1 ticket, nhiều ghế
            AddTicketRequest addRequest = new AddTicketRequest();
            addRequest.setSeat(new ArrayList<>());
            addRequest = AddTicketRequest.builder()
                    .customerName(customerName)
                    .customerPhone(customerPhone)
                    .tripId(tripId)
                    .bookerId(bookerId)
                    .ticketStatus(TicketStatus.BOOKED)
                    .invoices(invoice)          // 1 invoice cho 1 vé
                    .build();

            for (String s : listSeatIds) {
                if (!s.isEmpty()) {
                    addRequest.getSeat().add(Integer.parseInt(s)); // list seat id
                }
            }

            // 3️⃣ gọi service: tạo 1 Tickets chứa nhiều seats
            ticketService.addTicket(addRequest);

            CookieUtils.addCookie(response, "paymentStatus", String.valueOf(paymentStatus), "/", -1);
        } else {
            // payment fail → xoá giữ chỗ ghế
            for (String s : listSeatIds) {
                if (!s.isEmpty()) seatsService.deleteSeat(Integer.parseInt(s));
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
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