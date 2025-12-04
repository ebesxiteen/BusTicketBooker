package com.example.ticketbooker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.example.ticketbooker.Util.Utils.HMACUtil;

class HMACUtilTest {

    @Test
    void hmacHexStringEncodeGeneratesExpectedDigest() {
        String result = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, "secret", "payload");
        assertEquals("b29c5fba0fba4a11b6c650b882cf99cc372a129c4dc51dc5b835d850c7ece928", result);
    }

    @Test
    void hmacHexStringEncodeThrowsRuntimeExceptionForInvalidAlgorithm() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> HMACUtil.HMacHexStringEncode("unknown", "key", "data"));

        assertEquals("Error HMAC encryption", exception.getMessage());
    }
}
