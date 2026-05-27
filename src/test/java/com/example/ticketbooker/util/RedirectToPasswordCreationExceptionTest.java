package com.example.ticketbooker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example.ticketbooker.Util.RedirectToPasswordCreationException;

class RedirectToPasswordCreationExceptionTest {

    @Test
    void storesRedirectUrlAndMessage() {
        RedirectToPasswordCreationException exception = new RedirectToPasswordCreationException("/create");

        assertEquals("/create", exception.getRedirectUrl());
        assertEquals("Redirect to password creation: /create", exception.getMessage());
    }
}
