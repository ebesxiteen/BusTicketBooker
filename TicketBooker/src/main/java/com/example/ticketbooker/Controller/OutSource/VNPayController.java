package com.example.ticketbooker.Controller.OutSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ticketbooker.Service.OutSource.VNPAYService;
import com.example.ticketbooker.Util.Utils.CookieUtils;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/vnpay")
public class VNPayController {

    @Autowired
    private VNPAYService vnPayService;

    // =========================
    // 1. HIỂN THỊ TRANG TẠO ĐƠN (createOrder.html)
    // =========================
    @GetMapping("/create-order")
    public String showCreateOrder(HttpServletRequest request, Model model) {
        // Lấy tổng tiền từ cookie do booking.js set
        String grandTotalStr = CookieUtils.getCookieValue(request, "grandTotal", "0");
        int grandTotal = 0;
        try {
            grandTotal = Integer.parseInt(grandTotalStr);
        } catch (NumberFormatException e) {
            grandTotal = 0;
        }

        model.addAttribute("grandTotal", grandTotal);
        return "View/User/Basic/createOrder"; 
    }

    // =========================
    // 2. SUBMIT ĐƠN → TẠO LINK VNPay → REDIRECT SANG CỔNG VNPay
    // =========================
    @PostMapping("/submitOrder")
    public String submitOrder(@RequestParam("amount") int orderTotal,
                              @RequestParam("orderInfo") String orderInfo,
                              HttpServletRequest request) {

        // baseUrl: vd http://localhost:8000
        String baseUrl = request.getScheme() + "://" 
                       + request.getServerName() + ":" 
                       + request.getServerPort();

        // Tạo URL thanh toán VNPay (service bà đã có)
        String vnpayUrl = vnPayService.createOrder(request, orderTotal, orderInfo, baseUrl);

        return "redirect:" + vnpayUrl;
    }

    // =========================
    // 3. VNPay GỌI VỀ RETURN URL (USER ĐÃ THANH TOÁN XONG)
    // =========================
    @GetMapping("/return")
    public String paymentCompleted(HttpServletRequest request) {

        // vnPayService.orderReturn trả về 1 nếu thành công, 0 nếu thất bại /
        int paymentStatus = vnPayService.orderReturn(request); // 1 = success, 0 = fail

        // Không xử lý Invoice/Ticket ở đây nữa,
        // mà để /greenbus/thankyou dùng paymentStatus + cookies để xử lý
        return "redirect:/greenbus/thankyou?paymentStatus=" + paymentStatus;
    }

}
