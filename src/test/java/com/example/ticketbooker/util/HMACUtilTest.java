package com.example.ticketbooker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.example.ticketbooker.Util.Utils.HMACUtil;

class HMACUtilTest {

    @Test
    void hmacHexStringEncodeGeneratesExpectedDigest() {
        String result = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, "secret", "payload");
        assertEquals("b82fcb791acec57859b989b430a826488ce2e479fdf92326bd0a2e8375a42ba4", result);
    }

    @Test
    void hmacHexStringEncodeThrowsRuntimeExceptionForInvalidAlgorithm() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> HMACUtil.HMacHexStringEncode("unknown", "key", "data"));

        assertEquals("Error HMAC encryption", exception.getMessage());
    }
}
