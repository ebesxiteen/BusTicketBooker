package com.example.ticketbooker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @Test
    void handleIllegalArgumentReturnsApiBadRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/bad");

        ResponseEntity<?> response = (ResponseEntity<?>) handler.handleIllegalArgumentException(
                new IllegalArgumentException("bad input"), new ExtendedModelMap(), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiError body = (ApiError) response.getBody();
        assertEquals("BAD_REQUEST", body.code());
        assertEquals("bad input", body.message());
    }

    @Test
    void handleIllegalArgumentReturnsErrorViewForWebRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/page");

        ModelAndView modelAndView = (ModelAndView) handler.handleIllegalArgumentException(
                new IllegalArgumentException("bad input"), new ExtendedModelMap(), request);

        assertEquals("View/Util/404Page", modelAndView.getViewName());
        assertTrue(modelAndView.getModel().containsKey("error"));
    }

    @Test
    void handleExceptionReturnsGenericWebErrorView() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/error");

        ModelAndView modelAndView = (ModelAndView) handler.handleException(
                new IllegalStateException("boom"), new ExtendedModelMap(), request);

        assertEquals("View/Util/404Page", modelAndView.getViewName());
        assertTrue(modelAndView.getModel().containsKey("error"));
    }

    @Test
    void handleValidationExceptionReturnsApiFieldError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/tickets");
        MethodArgumentNotValidException exception = validationException("customerName", "must not be blank");

        ResponseEntity<?> response = (ResponseEntity<?>) handler.handleValidationException(
                exception, new ExtendedModelMap(), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiError body = (ApiError) response.getBody();
        assertEquals("VALIDATION_ERROR", body.code());
        assertEquals("customerName: must not be blank", body.message());
    }

    @Test
    void handleValidationExceptionReturnsWebErrorView() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/tickets");

        ModelAndView modelAndView = (ModelAndView) handler.handleValidationException(
                validationException("customerName", "must not be blank"), new ExtendedModelMap(), request);

        assertEquals("View/Util/404Page", modelAndView.getViewName());
        assertTrue(modelAndView.getModel().containsKey("error"));
    }

    private MethodArgumentNotValidException validationException(String field, String message) throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", field, message));
        Method method = SampleController.class.getDeclaredMethod("handle", String.class);
        return new MethodArgumentNotValidException(new MethodParameter(method, 0), bindingResult);
    }

    private static class SampleController {
        @SuppressWarnings("unused")
        void handle(String request) {
        }
    }
}
