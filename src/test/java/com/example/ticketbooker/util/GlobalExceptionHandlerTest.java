package com.example.ticketbooker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.ticketbooker.DTO.ApiError;
import com.example.ticketbooker.Util.GlobalExceptionHandler;
import com.example.ticketbooker.Util.RedirectToPasswordCreationException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleRedirectToPasswordCreationExceptionBuildsRedirect() {
        String view = handler.handleRedirectToPasswordCreationException(
                new RedirectToPasswordCreationException("/create-password"), new ExtendedModelMap());

        assertEquals("redirect:/create-password", view);
    }

    @Test
    void handleExceptionReturnsApiNotFoundResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/missing");

        ResponseEntity<?> response = (ResponseEntity<?>) handler.handleException(
                new NoHandlerFoundException("GET", "/api/missing", null), new ExtendedModelMap(), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiError body = (ApiError) response.getBody();
        assertEquals("NOT_FOUND", body.code());
        assertEquals("API not found", body.message());
    }

    @Test
    void handleExceptionReturnsGenericApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/error");

        ResponseEntity<?> response = (ResponseEntity<?>) handler.handleException(
                new IllegalStateException("boom"), new ExtendedModelMap(), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiError body = (ApiError) response.getBody();
        assertEquals("INTERNAL_SERVER_ERROR", body.code());
        assertEquals("Internal server error", body.message());
    }

    @Test
    void handleExceptionReturnsNotFoundViewForWebRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/web/page");

        ModelAndView modelAndView = (ModelAndView) handler.handleException(
                new NoHandlerFoundException("GET", "/web/page", null), new ExtendedModelMap(), request);

        assertEquals("View/Util/404Page", modelAndView.getViewName());
        assertTrue(modelAndView.getModel().containsKey("error"));
    }
}
