package com.example.ticketbooker.Controller;

import java.time.LocalDateTime;
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
            // Sửa: Lấy Users thay vì Account
            Users user = SecurityUtils.extractUser(principal);
            
            if (user != null) {
                model.addAttribute("fullname", user.getFullName());
                
                // Kiểm tra quyền Admin/Manager (Sửa logic so sánh role)
                boolean isAdmin = "ADMIN".equals(user.getRole()) || "MANAGER".equals(user.getRole());
                model.addAttribute("isAdmin", isAdmin);
            }
        }
        
        model.addAttribute("searchData", new SearchTripRequest());
        model.addAttribute("isLoggedIn", isLoggedIn);
        return "View/User/Basic/TrangChu";
    }

    @GetMapping("/about-us")
    public String showAboutUs() {
        return "View/User/Basic/AboutUs";
    }

    @GetMapping("/contact-us")
    public String showContactUs() {
        return "View/User/Basic/ContactUs";
    }

    // 2. TRANG TÌM CHUYẾN
    @GetMapping("/find-trip")
    public String showtrip(@ModelAttribute("responseTripDTO") ResponseTripDTO responseTripDTO, Model model) {
        if (!model.containsAttribute("responseTripDTO")) {
            model.addAttribute("responseTripDTO", new ResponseTripDTO());
        }
        LocalDateTime currentDate = LocalDateTime.now();
        model.addAttribute("currentDate", currentDate);
        return "View/User/Basic/FindTrip";
    }

    @PostMapping("/search-trips")
    public String searchTrips(@ModelAttribute("searchData") SearchTripRequest searchTripRequest, RedirectAttributes redirectAttributes) {
        ResponseTripDTO responseTripDTO = tripService.searchTrip(searchTripRequest);
        redirectAttributes.addFlashAttribute("responseTripDTO", responseTripDTO);
        return "redirect:/greenbus/find-trip";
    }

    // 3. TRANG ĐẶT VÉ (BOOKING)
    @GetMapping("/booking")
    public String showBooking(@RequestParam int tripId, Model model, HttpServletResponse response) {
        model.addAttribute("bookingInformation", tripService.getTripById(tripId));
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = SecurityUtils.isLoggedIn();
        
        if (isLoggedIn) {
            // Sửa: Lấy thông tin từ Users
            Users user = SecurityUtils.extractUser(authentication.getPrincipal());
            if (user != null) {
                Integer userId = user.getId();
                model.addAttribute("accountId", userId); // Giữ tên biến view cũ là accountId cho đỡ phải sửa HTML
                model.addAttribute("fullname", user.getFullName());
                model.addAttribute("phone", user.getPhone());
                model.addAttribute("email", user.getEmail());
                
                // Lưu cookie userId (thay vì bookerId cũ)
                CookieUtils.addCookie(response, "bookerId", Integer.toString(userId), "/", -1);
            }
        }
        return "View/User/Basic/Booking";
    }

    @GetMapping("/paying")
    public String showPaying() {
        return "View/User/Basic/Paying";
    }

    @GetMapping("/ticket-lookup")
    public String showTicketLookup() {
        return "View/User/Basic/LookUpTicket";
    }

    // 4. TRANG CẢM ƠN & XỬ LÝ VÉ (QUAN TRỌNG)
    @GetMapping("/thankyou")
    public String showPaymentSuccess(HttpServletRequest request, HttpServletResponse response, Model model, @RequestParam Map<String, String> allParams) {
        try {
            int tripId = Integer.parseInt(CookieUtils.getCookieValue(request, "tripId", "0"));
            String customerName = CookieUtils.getCookieValue(request, "customerName", "");
            
            // Lấy User ID (Booker)
            int bookerId = Integer.parseInt(CookieUtils.getCookieValue(request, "bookerId", "0"));
            
            String grandTotal = CookieUtils.getCookieValue(request, "grandTotal", "0");
            String email = CookieUtils.getCookieValue(request, "email", "");
            String customerPhone = CookieUtils.getCookieValue(request, "customerPhone", "");
            String seadIds = CookieUtils.getCookieValue(request, "seatIds", "") + " ";
            String[] seatIdList = seadIds.trim().split(" ");

            String paymentStatusParam = allParams.get("paymentStatus");
            int paymentStatus = (paymentStatusParam != null) ? Integer.parseInt(paymentStatusParam) : 0;

            if (paymentStatus == 1) { // Thanh toán thành công
                // Tạo hóa đơn
                AddInvoiceDTO addInvoiceDTO = new AddInvoiceDTO(
                        Integer.parseInt(grandTotal),
                        PaymentStatus.PAID,
                        LocalDateTime.now(),
                        PaymentMethod.EWALLET
                );
                int invoiceCreated = invoiceService.addInvoice(addInvoiceDTO);

                // Tạo vé cho từng ghế
                for (String s : seatIdList) {
                    if (s.isEmpty()) continue;
                    
                    AddTicketRequest addRequest = new AddTicketRequest();
                    addRequest.setCustomerName(customerName);
                    addRequest.setCustomerPhone(customerPhone);
                    addRequest.setTrip(tripService.getTripById(tripId).getListTrips().get(0));
                    addRequest.setSeat(seatsService.getSeatById(Integer.parseInt(s)));
                    
                    // --- SỬA LOGIC SET BOOKER ---
                    if (bookerId != 0) {
                        // Tạo object Users giả để làm Foreign Key (Hibernate tự hiểu)
                        // Không cần query lại DB để lấy full user, tiết kiệm tài nguyên
                        Users booker = Users.builder().id(bookerId).build();
                        addRequest.setBooker(booker);
                    }
                    // ----------------------------

                    addRequest.setTicketStatus(TicketStatus.BOOKED);
                    addRequest.setInvoices(invoiceService.getById(invoiceCreated));
                    ticketService.addTicket(addRequest);
                }
                CookieUtils.addCookie(response, "paymentStatus", Integer.toString(paymentStatus), "/", -1);
            } else if (paymentStatus == 0) {
                // Xử lý khi thanh toán thất bại (Logic cũ của bạn là xóa ghế???)
                // Mình giữ nguyên logic của bạn, nhưng cẩn thận chỗ này nhé.
                for (String s : seatIdList) {
                    if (!s.isEmpty()) seatsService.deleteSeat(Integer.parseInt(s));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "View/User/Basic/Thankyou";
    }

    @GetMapping("/login")
    public String login() {
        return "View/Util/Login";
    }

    @GetMapping("/profile")
    public String showProfile() {
        return "View/User/Registered/Profile/TicketHistory";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "View/Util/ResetPassword";
    }
}