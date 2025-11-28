package com.example.ticketbooker.Controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ticketbooker.DTO.Ticket.TicketResponse;
import com.example.ticketbooker.DTO.Users.UpdateUserRequest;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Repository.UserRepo;
import com.example.ticketbooker.Service.TicketService;
import com.example.ticketbooker.Service.UserService;
import com.example.ticketbooker.Util.SecurityUtils;
import com.example.ticketbooker.Util.Enum.TicketStatus;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final TicketService ticketService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;

    @Autowired
    public ProfileController(TicketService ticketService, 
                            UserService userService, 
                            PasswordEncoder passwordEncoder,
                            UserRepo userRepo) { 
        this.ticketService = ticketService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
    }

    // 1. Hiển thị thông tin
   @GetMapping("/info")
    public String showInfo(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Users currentUser = SecurityUtils.extractUser(authentication.getPrincipal());

    if (currentUser == null) {
        return "redirect:/auth";
    }

    // Lấy lại user mới nhất từ DB
    Users freshUser = userRepo.findById(currentUser.getId())
                              .orElseThrow(() -> new RuntimeException("User not found"));

    UpdateUserRequest form = userService.mapToUpdateUserRequest(freshUser);

    model.addAttribute("updateUserForm", form);
    model.addAttribute("fullName", freshUser.getFullName());
    
    return "View/User/Registered/Profile/Info";
    }
   // 2. Cập nhật thông tin
@PostMapping("/info/updates")
    public String updateUser(@ModelAttribute("updateUserForm") UpdateUserRequest updateUserForm,
                            RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users currentUser = SecurityUtils.extractUser(authentication.getPrincipal());

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không xác định được người dùng.");
            return "redirect:/profile/info";
        }

        // luôn update đúng user đang login
        updateUserForm.setUserId(currentUser.getId());
        // không cho đổi email ở đây: lấy lại từ user hiện tại
        updateUserForm.setEmail(currentUser.getEmail());

        try {
            userService.updateUser(updateUserForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thông tin thất bại.");
        }

        return "redirect:/profile/info";
    }
    @GetMapping("/info/updates")
    public String redirectInfoUpdate() {
        return "redirect:/profile/info";
    }

    // 3. Lịch sử đặt vé
    @GetMapping("/history-booking")
    public String showHistoryBooking(@RequestParam(required = false) Integer ticketId,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
                                    @RequestParam(required = false) String route,
                                    @RequestParam(required = false) TicketStatus status,
                                    Model model) {
        
        if (!SecurityUtils.isLoggedIn()) {
            return "redirect:/greenbus";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = SecurityUtils.extractUser(authentication.getPrincipal());
        
        TicketResponse ticketResponse = ticketService.searchTickets(user.getId(), ticketId, departureDate, route, status);
        
        model.addAttribute("ticketResponse", ticketResponse);
        model.addAttribute("ticketStatuses", TicketStatus.values());
        
        return "View/User/Registered/Profile/TicketHistory";
    }

    @GetMapping("/address")
    public String showAddress() {
        return "View/User/Registered/Profile/Address";
    }

    // 4. Trang đổi mật khẩu
    @GetMapping("/change-password")
    public String showChangePassword() {
        return "View/User/Registered/Profile/Password";
    }

    // 5. XỬ LÝ ĐỔI MẬT KHẨU
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam String newPassword, @RequestParam String oldPassword) {
        if (!SecurityUtils.isLoggedIn()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập lại!");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Lấy User entity trực tiếp từ SecurityUtils (đã chứa thông tin từ DB)
        Users user = SecurityUtils.extractUser(authentication.getPrincipal());

        try {
            // A. Kiểm tra mật khẩu cũ (dùng passwordEncoder.matches)
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu cũ không chính xác!");
            }

            // B. Kiểm tra trùng
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu mới không được giống mật khẩu cũ!");
            }

            // C. Mã hóa & Lưu xuống DB
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);
            
            userRepo.save(user);

            return ResponseEntity.ok("Đổi mật khẩu thành công!");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}