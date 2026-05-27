package com.example.ticketbooker.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.ticketbooker.DTO.ApiError;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RedirectToPasswordCreationException.class)
    @ResponseStatus(HttpStatus.FOUND)
    public String handleRedirectToPasswordCreationException(RedirectToPasswordCreationException ex, Model model) {
        return "redirect:" + ex.getRedirectUrl();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationException(MethodArgumentNotValidException ex, Model model, HttpServletRequest request) {
        if (!isApiRequest(request)) {
            model.addAttribute("errorMessage", "Invalid request data");
            return new ModelAndView("View/Util/404Page", "error", model);
        }

        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid request data");

        return ResponseEntity
                .badRequest()
                .body(ApiError.of("VALIDATION_ERROR", message, request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgumentException(IllegalArgumentException ex, Model model, HttpServletRequest request) {
        if (!isApiRequest(request)) {
            model.addAttribute("errorMessage", ex.getMessage());
            return new ModelAndView("View/Util/404Page", "error", model);
        }

        return ResponseEntity
                .badRequest()
                .body(ApiError.of("BAD_REQUEST", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, Model model, HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (isApiRequest(request)) {
            if (ex instanceof NoHandlerFoundException) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiError.of("NOT_FOUND", "API not found", uri));
            }

            log.error("Unhandled API exception at {}", uri, ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiError.of("INTERNAL_SERVER_ERROR", "Internal server error", uri));
        }

        if (ex instanceof NoHandlerFoundException) {
            model.addAttribute("errorMessage", "Page not found");
            return new ModelAndView("View/Util/404Page", "error", model);
        }

        log.error("Unhandled web exception at {}", uri, ex);
        model.addAttribute("errorMessage", "Something went wrong");
        return new ModelAndView("View/Util/404Page", "error", model);
    }

    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn() {
        return SecurityUtils.isLoggedIn();
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }
}
