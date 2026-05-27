package com.example.ticketbooker.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Repository.UserRepo;
import com.example.ticketbooker.Util.SecurityUtils;

@Controller
public class PasswordCreationController {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordCreationController(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/new-password")
    public String showPasswordCreationPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (SecurityUtils.isLoggedIn()) {
            Users user = SecurityUtils.extractUser(authentication.getPrincipal());
            if (user != null) {
                model.addAttribute("email", user.getEmail());
                model.addAttribute("fullname", user.getFullName());
                return "View/Util/NewPassword";
            }
        }
        return "redirect:/greenbus";
    }

    @PostMapping("/new-password")
    public String handlePasswordCreation(@RequestParam String password) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (SecurityUtils.isLoggedIn()) {
            Users currentUser = SecurityUtils.extractUser(authentication.getPrincipal());
            
            if (currentUser != null) {
                try {
                    // Mã hóa và cập nhật mật khẩu (Hàm encode là của interface PasswordEncoder)
                    String encodedPassword = passwordEncoder.encode(password); 
                    currentUser.setPassword(encodedPassword);
                    
                    userRepo.save(currentUser); // Lưu vào bảng Users
                    
                    System.out.println("Đã cập nhật mật khẩu cho user: " + currentUser.getEmail());
                    
                    return "redirect:/greenbus"; 
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    return "redirect:/new-password?error";
                }
            }
        }
        return "redirect:/greenbus";
    }
}