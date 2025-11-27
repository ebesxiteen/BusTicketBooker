(function () {
    document.addEventListener("DOMContentLoaded", async function () {
        
        // 1. Lấy và xử lý Tổng tiền (An toàn hơn)
        const totalElement = document.getElementById("grandTotal");
        if (!totalElement) return; // Nếu không ở trang thanh toán thì dừng lại

        // Xóa tất cả ký tự không phải số (dấu phẩy, dấu chấm, chữ đ)
        const total = totalElement.innerText.replace(/[^0-9]/g, ""); 

        // 2. Sửa lỗi cú pháp .value() -> .value
        // Lưu ý: Đảm bảo các input này có class 'apiField' và đúng name trong HTML
        const fullNameInput = document.querySelector(".apiField[name='fullname']");
        const phoneInput = document.querySelector(".apiField[name='phone']");
        const emailInput = document.querySelector(".apiField[name='email']");

        const fullName = fullNameInput ? fullNameInput.value : "";
        const phone = phoneInput ? phoneInput.value : "";
        const email = emailInput ? emailInput.value : "";

        const description = "Thanh toan GreenBus " + Date.now();
        console.log("Total Amount:", total);

        try {
            // 3. Gọi API tạo đơn hàng (Dùng đường dẫn tương đối)
            const response = await fetch("/payment/zalo-payment", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ 
                    appUser: fullName, 
                    amount: total, 
                    description: description,
                    phone: phone, // Gửi thêm nếu backend cần
                    email: email 
                })
            });

            const data = await response.json();

            if (data.returnCode === 1) {
                console.log("Tạo đơn thành công, đang chuyển hướng...");
                
                // (Tùy chọn) Gọi API lưu trạng thái trước khi chuyển đi
                // Lưu ý: Thường bước này backend tự xử lý khi nhận callback từ ZaloPay
                /*
                await fetch("/payment/zalo-payment-status", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(data)
                });
                */

                // 4. Chuyển hướng sang cổng thanh toán ZaloPay
                window.location.href = data.returnUrl;
            } else {
                console.error("Lỗi tạo đơn ZaloPay:", data.returnMessage);
                alert("Lỗi thanh toán: " + data.returnMessage);
            }

        } catch (e) {
            console.error("Lỗi kết nối:", e);
            alert("Không thể kết nối đến máy chủ thanh toán.");
        }
    });
})();