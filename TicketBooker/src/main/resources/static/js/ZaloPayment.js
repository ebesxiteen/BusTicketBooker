// /static/js/ZaloPayment.js

async function startZaloPay(tripId, selectedSeats) {
    try {
        // 1. Lấy thông tin cần thiết
        const totalElement = document.getElementById("grandTotal");
        if (!totalElement) {
            Swal.fire({
                icon: 'error',
                title: 'Lỗi giao diện',
                text: 'Không tìm thấy tổng tiền (#grandTotal).'
            });
            return;
        }

        const rawAmount = totalElement.innerText.replace(/[^0-9]/g, "");
        const amount = parseInt(rawAmount || "0", 10);

        const fullNameInput = document.querySelector('input[name="customerName"]');
        const phoneInput = document.querySelector('input[name="customerPhone"]');
        const emailInput = document.querySelector('input[name="email"]');

        const appUser = fullNameInput ? fullNameInput.value : "";
        const phone = phoneInput ? phoneInput.value : "";
        const email = emailInput ? emailInput.value : "";

        const description = "Thanh toán vé GreenBus " + Date.now();

        // 2. GỌI API ZALOPAY ĐỂ TẠO ĐƠN HÀNG (API Backend /payment/zalo-payment)
        const res = await fetch("/payment/zalo-payment", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                appUser: appUser,
                amount: amount,
                description: description,
                phone: phone,
                email: email
            })
        });

        const text = await res.text();
        let data;
        try {
            data = JSON.parse(text);
        } catch (e) {
            Swal.fire({
                icon: 'error',
                title: 'Lỗi server ZaloPay',
                text: 'Server đang trả về dữ liệu không hợp lệ. Vui lòng kiểm tra lại Network tab và log server Java.'
            });
            return;
        }

        // 3. KIỂM TRA KẾT QUẢ TẠO ĐƠN
        if (data.returnCode === 1 && data.returnUrl) {
            
            // 3.1. TẠO ĐƠN ZALOPAY THÀNH CÔNG -> GỌI API TẠO GHẾ TẠM
            
            // Dữ liệu tạo ghế tạm (/api/seats/add cần tripId và seatCode là chuỗi "A01 A02")
            const seatPayload = {
                tripId: parseInt(tripId), 
                seatCode: selectedSeats.join(' ') 
            };

            // GỌI API TẠO GHẾ TẠM CŨ CỦA BẠN (SeatsApi.java)
            const seatRes = await fetch("/api/seats/add", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(seatPayload)
            });
            
            if (seatRes.ok) {
                // 3.2. ĐẶT CHỖ THÀNH CÔNG -> Redirect sang ZaloPay
                window.location.href = data.returnUrl;
            } else {
                // Lỗi 4xx/5xx từ /api/seats/add (Ví dụ: ghế đã bị người khác đặt/Lỗi DB)
                const errorText = await seatRes.text();
                Swal.fire({
                    icon: 'error',
                    title: 'Lỗi đặt chỗ tạm thời',
                    // Hiển thị message lỗi chi tiết từ SeatsApi.java (nếu có)
                    text: "Ghế không thể đặt chỗ. Có thể ghế đã bị người khác chọn. Chi tiết: " + errorText 
                });
                // ⚠️ Quan trọng: Đơn ZaloPay đã được tạo nhưng sẽ hết hạn sau 15 phút.
                // Chúng ta không cần làm gì thêm ở đây.
            }
            
        } else {
            // LỖI TỪ ZALOPAY (Return Code != 1) - Ghế chưa được tạo
            Swal.fire({
                icon: 'error',
                title: 'Tạo đơn ZaloPay thất bại',
                // data.detailMessage chứa thông báo lỗi sai MAC, Key, Port...
                text: "Chi tiết lỗi: " + (data.detailMessage || "Không xác định")
            });
        }
    } catch (e) {
        console.error("Lỗi trong startZaloPay: ", e);
        Swal.fire({
            icon: 'error',
            title: 'Lỗi kết nối',
            text: 'Không thể kết nối với cổng thanh toán. Vui lòng kiểm tra lại mạng.'
        });
    }
}