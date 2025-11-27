// --- CẤU HÌNH MÀU SẮC GREENBUS ---
tailwind.config = {
    theme: {
        extend: {
            colors: {
                primary: '#10b981',   /* Xanh ngọc */
                secondary: '#047857', /* Xanh đậm */
                accent: '#d1fae5',    /* Xanh nhạt */
            }
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    
    // Hàm gọi API Tra cứu vé
    async function fetchTicketInfo() {
        const ticketId = document.getElementById("ticketId").value.trim();
        const customerPhone = document.getElementById("phoneNumber").value.trim();

        // Validate
        if (!ticketId) {
            Swal.fire({ icon: 'warning', title: 'Vui lòng nhập mã vé!' });
            return;
        }
        if (!customerPhone) {
            Swal.fire({ icon: 'warning', title: 'Vui lòng nhập số điện thoại!' });
            return;
        }

        try {
            // SỬA URL: Dùng đường dẫn tương đối
            const response = await fetch("/api/tickets/payment-infor", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ ticketId, customerPhone })
            });

            if (!response.ok) {
                if(response.status === 404) throw new Error("Không tìm thấy vé. Vui lòng kiểm tra lại!");
                throw new Error("Có lỗi xảy ra khi tra cứu.");
            }

            const ticketInfo = await response.json();

            // Hiển thị dữ liệu (Mapping)
            const elements = {
                "ticketIdValue": ticketInfo.id,
                "customerNameValue": ticketInfo.customerName,
                "customerPhoneValue": ticketInfo.customerPhone,
                "bookingDateValue": ticketInfo.paymentTime, // Định dạng ngày tháng nếu cần
                "emailValue": ticketInfo.email,
                "totalPriceValue": new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(ticketInfo.totalAmount)
            };

            for (const [id, value] of Object.entries(elements)) {
                const element = document.getElementById(id);
                if (element) element.textContent = value;
            }

            // Hiển thị lộ trình
            const tripInfoElements = {
                ".tripInfo .departure .time": ticketInfo.departureTime,
                ".tripInfo .departure .location": ticketInfo.departureLocation,
                ".tripInfo .arrival .time": ticketInfo.arrivalTime,
                ".tripInfo .arrival .location": ticketInfo.arrivalLocation
            };

            for (const [selector, value] of Object.entries(tripInfoElements)) {
                const element = document.querySelector(selector);
                if (element) element.textContent = value;
            }

            // Chuyển đổi giao diện (Ẩn tìm kiếm -> Hiện kết quả)
            toggleSection(true);

        } catch (error) {
            Swal.fire({ icon: 'error', title: 'Lỗi', text: error.message });
        }
    }

    // Hàm Reset Form
    function resetForm() {
        document.getElementById("ticketId").value = "";
        document.getElementById("phoneNumber").value = "";
        toggleSection(false);
    }

    // Hàm ẩn hiện section
    function toggleSection(isShowResult) {
        const searchSection = document.getElementById("searchSection");
        const ticketInfoElement = document.getElementById("ticketInfo");

        if (isShowResult) {
            searchSection.classList.add("hidden");
            ticketInfoElement.classList.remove("hidden");
        } else {
            searchSection.classList.remove("hidden");
            ticketInfoElement.classList.add("hidden");
        }
    }

    // Gán sự kiện (Event Listener)
    // Lưu ý: Nên gán ID cho nút thay vì query theo onclick
    const searchButton = document.querySelector('button[onclick="fetchTicketInfo()"]') || document.getElementById('btnSearchTicket');
    if (searchButton) {
        searchButton.onclick = function(e) {
            e.preventDefault(); // Ngăn reload form
            fetchTicketInfo();
        };
    }

    const resetButton = document.querySelector('button[onclick="resetForm()"]') || document.getElementById('btnResetTicket');
    if (resetButton) {
        resetButton.onclick = function(e) {
            e.preventDefault();
            resetForm();
        };
    }
});