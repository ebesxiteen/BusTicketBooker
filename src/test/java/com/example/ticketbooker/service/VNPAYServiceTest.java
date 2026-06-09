package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.ticketbooker.Config.VNPAYConfig;
import com.example.ticketbooker.Service.OutSource.VNPAYService;

class VNPAYServiceTest {

    private final VNPAYService vnpayService = new VNPAYService();
    private static final String HASH_SECRET = "dummy-vnpay-hash-secret";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vnpayService, "payUrl", VNPAYConfig.vnp_PayUrl);
        ReflectionTestUtils.setField(vnpayService, "returnPath", VNPAYConfig.vnp_Returnurl);
        ReflectionTestUtils.setField(vnpayService, "tmnCode", "dummy-vnpay-tmn-code");
        ReflectionTestUtils.setField(vnpayService, "hashSecret", HASH_SECRET);
        VNPAYConfig.vnp_HashSecret = HASH_SECRET;
    }

    @Test
    void createOrderBuildsPaymentUrlWithAmountReturnUrlAndSecureHash() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalAddr("127.0.0.1");

        String url = vnpayService.createOrder(request, 1000, "ORDER 1", "http://localhost:8000");

        assertTrue(url.startsWith(VNPAYConfig.vnp_PayUrl + "?"));
        assertTrue(url.contains("vnp_Amount=100000"));
        assertTrue(url.contains("vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8000%2Fvnpay%2Freturn"));
        assertTrue(url.contains("vnp_SecureHash="));
    }

    @Test
    void orderReturnReturnsOneForValidSignatureAndSuccessStatus() {
        MockHttpServletRequest request = signedRequest("00");

        assertEquals(1, vnpayService.orderReturn(request));
    }

    @Test
    void orderReturnReturnsZeroForValidSignatureAndFailedStatus() {
        MockHttpServletRequest request = signedRequest("01");

        assertEquals(0, vnpayService.orderReturn(request));
    }

    @Test
    void orderReturnReturnsMinusOneForInvalidSignature() {
        MockHttpServletRequest request = signedRequest("00");
        request.setParameter("vnp_SecureHash", "invalid");

        assertEquals(-1, vnpayService.orderReturn(request));
    }

    private MockHttpServletRequest signedRequest(String status) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("vnp_Amount", "100000");
        request.setParameter("vnp_TxnRef", "12345678");
        request.setParameter("vnp_TransactionStatus", status);

        Map<String, String> fields = new HashMap<>();
        fields.put("vnp_Amount", "100000");
        fields.put("vnp_TxnRef", "12345678");
        fields.put("vnp_TransactionStatus", status);
        request.setParameter("vnp_SecureHash", VNPAYConfig.hashAllFields(fields));
        return request;
    }
}
