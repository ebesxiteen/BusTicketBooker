package com.example.ticketbooker.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // Khi vào /admin -> Chuyển hướng ngay sang /admin/statistics
    @GetMapping("") 
    public String index() {
        return "redirect:/admin/statistics"; 
    }
}