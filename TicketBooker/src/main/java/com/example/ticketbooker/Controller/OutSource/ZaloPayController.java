package com.example.ticketbooker.Controller.OutSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketbooker.DTO.OutSource.ZaloPaymentRequest;
import com.example.ticketbooker.DTO.OutSource.ZaloPaymentResponse;
import com.example.ticketbooker.DTO.OutSource.ZaloPaymentStatusResponse;
import com.example.ticketbooker.Service.OutSource.ZaloPayService;

@RestController
@RequestMapping("/payment")
public class ZaloPayController {

    @Autowired
    private ZaloPayService zaloPayService;

    @PostMapping(value = "/zalo-payment",
                 consumes = "application/json",
                 produces = "application/json")
    public ResponseEntity<ZaloPaymentResponse> zaloPayment(@RequestBody ZaloPaymentRequest request) {
        try {
            ZaloPaymentResponse res = zaloPayService.requestPayment(request);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            ZaloPaymentResponse error = new ZaloPaymentResponse();
            error.setReturnCode(-1);
            error.setDetailMessage("Lỗi server: " + e.getMessage());
            error.setReturnUrl(null);
            error.setPaymentId(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping(value = "/zalo-payment-status",
                 consumes = "application/json",
                 produces = "application/json")
    public ResponseEntity<ZaloPaymentStatusResponse> zaloPaymentStatus(
            @RequestBody ZaloPaymentResponse response) {
        if (response.getReturnCode() == 1) {
            while (true) {
                try {
                    ZaloPaymentStatusResponse paymentStatus =
                            zaloPayService.requestPaymentStatus(response.getPaymentId());
                    if (!paymentStatus.isProcessing()) {
                        return ResponseEntity.ok(paymentStatus);
                    }
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                    ZaloPaymentStatusResponse err = new ZaloPaymentStatusResponse();
                    err.setReturnCode(-1);
                    err.setReturnMessage("Lỗi server: " + e.getMessage());
                    err.setProcessing(false);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
                }
            }
        }
        return ResponseEntity.ok(new ZaloPaymentStatusResponse());
    }
}

