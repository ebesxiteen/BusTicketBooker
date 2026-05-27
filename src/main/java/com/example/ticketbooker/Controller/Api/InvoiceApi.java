package com.example.ticketbooker.Controller.Api;

import com.example.ticketbooker.DTO.Invoice.AddInvoiceDTO;
import com.example.ticketbooker.DTO.Invoice.RequestInvoiceDTO;
import com.example.ticketbooker.DTO.Invoice.ResponseInvoiceDTO;
import com.example.ticketbooker.DTO.Invoice.UpdateInvoiceStatusDTO;
import com.example.ticketbooker.Service.InvoiceService;
import com.example.ticketbooker.Util.Enum.PaymentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceApi {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/create")
    public int createInvoice(@RequestBody AddInvoiceDTO addInvoiceDTO) {
        int result = 0;
        try {
            result = invoiceService.addInvoice(addInvoiceDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @PostMapping("/search")
    public ResponseEntity<ResponseInvoiceDTO> searchInvoices(@RequestBody RequestInvoiceDTO requestDTO) {
        ResponseInvoiceDTO result = invoiceService.searchInvoices(requestDTO);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateInvoiceStatus(@PathVariable int id, @RequestBody UpdateInvoiceStatusDTO updateDTO) {
        PaymentStatus newStatus = updateDTO.getPaymentStatus();
        if (newStatus == null) {
            return ResponseEntity.badRequest().body("Trạng thái thanh toán không hợp lệ");
        }

        boolean updated = invoiceService.updatePaymentStatus(id, newStatus);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Không thể cập nhật trạng thái cho hóa đơn này");
        }

        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }
}
