package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.ticketbooker.DTO.OutSource.ZaloPaymentRequest;
import com.example.ticketbooker.DTO.OutSource.ZaloPaymentResponse;
import com.example.ticketbooker.DTO.OutSource.ZaloPaymentStatusResponse;
import com.example.ticketbooker.Service.OutSource.ZaloPayService;

@ExtendWith(MockitoExtension.class)
class ZaloPayServiceTest {

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private CloseableHttpResponse httpResponse;

    private TestableZaloPayService service;

    @BeforeEach
    void setUp() {
        service = new TestableZaloPayService(httpClient);
        ReflectionTestUtils.setField(service, "appId", "2553");
        ReflectionTestUtils.setField(service, "key", "secret");
        ReflectionTestUtils.setField(service, "expireTime", "900");
        ReflectionTestUtils.setField(service, "createEndpoint", "https://zalopay.test/create");
        ReflectionTestUtils.setField(service, "queryEndpoint", "https://zalopay.test/query");
    }

    @Test
    void requestPaymentPostsOrderAndMapsJsonResponse() throws Exception {
        when(httpResponse.getEntity()).thenReturn(new StringEntity("""
                {"return_code":1,"sub_return_message":"OK","order_url":"https://pay.test/order"}
                """, "UTF-8"));
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
        ArgumentCaptor<HttpPost> postCaptor = ArgumentCaptor.forClass(HttpPost.class);

        ZaloPaymentResponse result = service.requestPayment(new ZaloPaymentRequest("user-1", 100000, "Ticket payment"));

        assertEquals(1, result.getReturnCode());
        assertEquals("OK", result.getDetailMessage());
        assertEquals("https://pay.test/order", result.getReturnUrl());
        assertNotNull(result.getPaymentId());
        verify(httpClient).execute(postCaptor.capture());
        assertEquals("https://zalopay.test/create", postCaptor.getValue().getURI().toString());
    }

    @Test
    void requestPaymentStatusPostsQueryAndMapsJsonResponse() throws Exception {
        when(httpResponse.getEntity()).thenReturn(new StringEntity("""
                {"return_code":1,"sub_return_message":"Paid","is_processing":false}
                """, "UTF-8"));
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

        ZaloPaymentStatusResponse result = service.requestPaymentStatus("260529_123");

        assertEquals(1, result.getReturnCode());
        assertEquals("Paid", result.getReturnMessage());
        assertEquals(false, result.isProcessing());
    }

    private static class TestableZaloPayService extends ZaloPayService {
        private final CloseableHttpClient httpClient;

        private TestableZaloPayService(CloseableHttpClient httpClient) {
            this.httpClient = httpClient;
        }

        @Override
        protected CloseableHttpClient createHttpClient() {
            return httpClient;
        }
    }
}
