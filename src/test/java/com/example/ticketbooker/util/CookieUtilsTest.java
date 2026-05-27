package com.example.ticketbooker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.example.ticketbooker.Util.Utils.CookieUtils;

import jakarta.servlet.http.Cookie;

class CookieUtilsTest {

    @Test
    void getCookieValueReturnsDecodedValueWhenPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("token", "value%20123"));

        String result = CookieUtils.getCookieValue(request, "token", "default");

        assertEquals("value 123", result);
    }

    @Test
    void getCookieValueReturnsDefaultWhenMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = CookieUtils.getCookieValue(request, "token", "fallback");

        assertEquals("fallback", result);
    }

    @Test
    void addCookieAddsConfiguredCookieToResponse() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieUtils.addCookie(response, "sessionId", "abc", "/api", 10);

        Cookie cookie = response.getCookie("sessionId");
        assertEquals("abc", cookie.getValue());
        assertEquals("/api", cookie.getPath());
        assertEquals(10, cookie.getMaxAge());
    }
}
