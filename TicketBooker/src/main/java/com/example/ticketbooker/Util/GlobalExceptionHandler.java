package com.example.ticketbooker.Util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RedirectToPasswordCreationException.class)
    @ResponseStatus(HttpStatus.FOUND)
    public String handleRedirectToPasswordCreationException(RedirectToPasswordCreationException ex, Model model) {
        System.out.println("Check vào ham global exception handler");
        return "redirect:" + ex.getRedirectUrl();
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, Model model, HttpServletRequest request) {

        String uri = request.getRequestURI();
        boolean isApi = uri.startsWith("/api/");

        // Nếu là API request => trả JSON/text, không trả view
        if (isApi) {
            if (ex instanceof NoHandlerFoundException) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("API not found: " + uri);
            }

            // Các lỗi khác trong API
            ex.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Xin lỗi, hệ thống đang gặp sự cố. Bà thử lại sau giúp tui nha 🙏");
        }

        // Nếu KHÔNG phải API => xử lý như cũ, trả trang 404
        if (ex instanceof NoHandlerFoundException) {
            model.addAttribute("errorMessage", "Page not found");
            return new ModelAndView("View/Util/404Page", "error", model);
        }

        model.addAttribute("errorMessage", "Something went wrong");
        return new ModelAndView("View/Util/404Page", "error", model);
    }

    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn() {
        return SecurityUtils.isLoggedIn();
    }
}
