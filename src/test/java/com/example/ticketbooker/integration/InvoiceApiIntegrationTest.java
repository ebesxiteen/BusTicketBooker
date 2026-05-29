package com.example.ticketbooker.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ticketbooker.Controller.Api.InvoiceApi;
import com.example.ticketbooker.DTO.Invoice.ResponseInvoiceDTO;
import com.example.ticketbooker.Service.InvoiceService;
import com.example.ticketbooker.Util.Enum.PaymentStatus;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=dummy-test-key",
        "spring.ai.openai.base-url=https://example.com",
        "spring.ai.openai.chat.options.model=llama3-8b-8192",
        "ZALO_APP_ID=demo-app-id",
        "ZALO_KEY1=demo-key-1",
        "ZALO_KEY2=demo-key-2",
        "ZALO_ENDPOINT=https://sandbox.zalopay.vn/v001/tpe/createorder"
})
@AutoConfigureMockMvc(addFilters = false)
class InvoiceApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @Test
    void createInvoiceReturnsCreatedInvoiceId() throws Exception {
        when(invoiceService.addInvoice(any())).thenReturn(88);

        mockMvc.perform(post("/api/invoices/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"totalAmount": 250000, "paymentStatus": "PENDING", "paymentMethod": "VNPAY"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("88"));
    }

    @Test
    void searchInvoicesReturnsResponseDto() throws Exception {
        ResponseInvoiceDTO response = ResponseInvoiceDTO.builder()
                .invoicesCount(0)
                .build();
        when(invoiceService.searchInvoices(any())).thenReturn(response);

        mockMvc.perform(post("/api/invoices/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"paymentStatus": "PAID", "page": 0, "size": 10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoicesCount").value(0));
    }

    @Test
    void updateInvoiceStatusReturnsOkWhenServiceUpdates() throws Exception {
        when(invoiceService.updatePaymentStatus(88, PaymentStatus.PAID)).thenReturn(true);

        mockMvc.perform(put("/api/invoices/88/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"paymentStatus": "PAID"}
                                """))
                .andExpect(status().isOk());

        verify(invoiceService).updatePaymentStatus(88, PaymentStatus.PAID);
    }

    @Test
    void updateInvoiceStatusReturnsBadRequestWhenStatusMissing() throws Exception {
        mockMvc.perform(put("/api/invoices/88/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
