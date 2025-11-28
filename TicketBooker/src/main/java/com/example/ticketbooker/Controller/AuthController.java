package com.example.ticketbooker.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Repository.UserRepo;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepo userRepo; // Dùng UserRepo để tìm thông tin chi tiết

    // 1. Trang đăng nhập
    @GetMapping()
    public String login(Authentication authentication) {
        // Nếu đã đăng nhập rồi thì đá về trang chủ
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/greenbus"; // Hoặc trang home của bạn
        }
        return "View/Util/Login";
    }

    // 2. Trang Profile (Thông tin cá nhân)
    @GetMapping("/profile/")
    public String getUserProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/auth"; // Chưa đăng nhập thì về trang login
        }

        String email = null;
        Object principal = authentication.getPrincipal();

        // --- XỬ LÝ LẤY EMAIL TỪ PRINCIPAL (HỖ TRỢ CẢ GOOGLE VÀ LOCAL) ---
        if (principal instanceof UserDetails) {
            // Đăng nhập thường (Local)
            email = ((UserDetails) principal).getUsername(); // Trong cấu hình mới, username chính là email
        } else if (principal instanceof OAuth2User) {
            // Đăng nhập Google/Facebook
            email = ((OAuth2User) principal).getAttribute("email");
        }
        // ---------------------------------------------------------------

        if (email != null) {
            // Tìm User trong DB để lấy FullName, Role...
            Users user = userRepo.findByEmail(email);
            
            if (user != null) {
                model.addAttribute("name", user.getFullName());
                model.addAttribute("email", user.getEmail());
                model.addAttribute("role", user.getRole());
                model.addAttribute("phone", user.getPhone());
                // Truyền thêm user object nếu view cần
                model.addAttribute("user", user);
            }
        }

        return "View/User/Registered/Profile";
    }

    // 3. Xử lý lỗi đăng nhập
    @GetMapping(params = "error")
    public String loginError(@RequestParam(value = "error", required = false) Boolean error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Sai email hoặc mật khẩu!");
        }
        return "View/Util/Login";
    }
}