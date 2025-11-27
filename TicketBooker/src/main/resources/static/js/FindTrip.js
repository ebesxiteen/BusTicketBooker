(function () {
    document.addEventListener("DOMContentLoaded", function () {
        
        // ==========================================
        // 1. XỬ LÝ UI: Ẩn/Hiện Ngày Về (OneWay/RoundTrip)
        // ==========================================
        const tripTypeRadios = document.querySelectorAll('input[name="tripType"]');
        const returnDateContainer = document.getElementById('returnDateContainer');

        function updateDateContainerLayout() {
            // Kiểm tra null để tránh lỗi nếu trang không có phần này
            const selectedRadio = document.querySelector('input[name="tripType"]:checked');
            if (selectedRadio && returnDateContainer) {
                if (selectedRadio.value === 'oneWay') {
                    returnDateContainer.classList.add('hidden');
                } else {
                    returnDateContainer.classList.remove('hidden');
                }
            }
        }

        tripTypeRadios.forEach(radio => {
            radio.addEventListener('change', updateDateContainerLayout);
        });
        updateDateContainerLayout(); // Chạy lần đầu

        // ==========================================
        // 2. KHỞI TẠO LOGIC ĐẶT VÉ
        // ==========================================
        let TICKET_PRICE = 0;
        let selectedSeats = [];
        
        // Lấy TripId từ URL
        const urlParams = new URLSearchParams(window.location.search);
        const tripId = urlParams.get('tripId');

        if(tripId) {
            fetchTripDetails();
            bookingLogic();
            handlePayment();
        }

        // ==========================================
        // 3. LẤY THÔNG TIN CHUYẾN & GIÁ VÉ
        // ==========================================
        async function fetchTripDetails() {
            try {
                const response = await fetch(`/admin/trips/${tripId}`);
                if (response.ok) {
                    const data = await response.json();
                    
                    // Cập nhật UI thông tin chuyến
                    const locEl = document.getElementById('departureLocation');
                    const timeEl = document.getElementById('departureTime');
                    if(locEl) locEl.textContent = `${data.departureLocation} - ${data.arrivalLocation}`;
                    if(timeEl) timeEl.textContent = data.departureTime;

                    // Lưu giá vé
                    TICKET_PRICE = data.totalPrice || 0; // Đảm bảo field này khớp với API Trip
                    updatePriceInfo();
                }
            } catch (error) {
                console.error('Lỗi lấy thông tin chuyến:', error);
            }
        }

        // ==========================================
        // 4. LOGIC VẼ GHẾ & CHỌN GHẾ
        // ==========================================
        async function bookingLogic() {
            // Container chứa ghế (Bạn cần thêm div id="seatMapContainer" vào HTML)
            const seatContainer = document.getElementById('seatMapContainer');
            if(!seatContainer) return;

            try {
                // Gọi API lấy ghế đã đặt
                const response = await fetch(`/api/seats/${tripId}/booked`);
                let bookedSeats = [];
                if (response.ok) {
                    bookedSeats = await response.json();
                }

                // Vẽ sơ đồ ghế (Tầng A và Tầng B) - 18 ghế mỗi tầng
                renderSeatMap(seatContainer, bookedSeats);

            } catch (error) {
                console.error('Lỗi tải sơ đồ ghế:', error);
            }
        }

        function renderSeatMap(container, bookedSeats) {
            // HTML khung sơ đồ (Style GreenBus)
            container.innerHTML = `
                <div class="mt-4 p-4 border border-gray-200 rounded-lg bg-white shadow-sm">
                    <h3 class="text-lg font-semibold mb-4 text-gray-700">Chọn ghế</h3>
                    
                    <div class="flex justify-center gap-4 mb-6 text-sm">
                        <div class="flex items-center gap-2">
                            <div class="w-6 h-6 bg-gray-300 rounded"></div>
                            <span>Đã bán</span>
                        </div>
                        <div class="flex items-center gap-2">
                            <div class="w-6 h-6 border-2 border-green-500 bg-white rounded"></div>
                            <span>Còn trống</span>
                        </div>
                        <div class="flex items-center gap-2">
                            <div class="w-6 h-6 bg-green-500 rounded"></div>
                            <span>Đang chọn</span>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                        <div class="text-center">
                            <h4 class="font-medium mb-3 text-gray-600">Tầng dưới (A)</h4>
                            <div class="grid grid-cols-3 gap-3 justify-center max-w-[200px] mx-auto">
                                ${generateSeatHTML('A', 18, bookedSeats)}
                            </div>
                        </div>
                        
                        <div class="text-center">
                            <h4 class="font-medium mb-3 text-gray-600">Tầng trên (B)</h4>
                            <div class="grid grid-cols-3 gap-3 justify-center max-w-[200px] mx-auto">
                                ${generateSeatHTML('B', 18, bookedSeats)}
                            </div>
                        </div>
                    </div>
                </div>
            `;

            // Gán sự kiện click cho các ghế vừa tạo
            const seatButtons = container.querySelectorAll('.seat-btn');
            seatButtons.forEach(btn => {
                btn.addEventListener('click', function() {
                    handleSeatClick(this);
                });
            });
        }

        function generateSeatHTML(prefix, count, bookedSeats) {
            let html = '';
            for (let i = 1; i <= count; i++) {
                const seatCode = `${prefix}${i.toString().padStart(2, '0')}`; // VD: A01
                const isBooked = bookedSeats.includes(seatCode);
                
                // Style cho ghế
                let classList = "seat-btn w-10 h-10 rounded-lg text-xs font-bold transition-all duration-200 flex items-center justify-center ";
                
                if (isBooked) {
                    classList += "bg-gray-300 text-gray-500 cursor-not-allowed";
                } else {
                    classList += "bg-white border-2 border-green-500 text-green-600 hover:bg-green-50 cursor-pointer shadow-sm";
                }

                html += `<button type="button" 
                            class="${classList}" 
                            data-code="${seatCode}" 
                            ${isBooked ? 'disabled' : ''}>
                            ${seatCode}
                         </button>`;
            }
            return html;
        }

        function handleSeatClick(btn) {
            const seatCode = btn.dataset.code;

            if (selectedSeats.includes(seatCode)) {
                // Bỏ chọn -> Về trạng thái trống (Trắng viền xanh)
                selectedSeats = selectedSeats.filter(s => s !== seatCode);
                btn.className = "seat-btn w-10 h-10 rounded-lg text-xs font-bold transition-all duration-200 flex items-center justify-center bg-white border-2 border-green-500 text-green-600 hover:bg-green-50 cursor-pointer shadow-sm";
            } else {
                // Chọn mới
                if (selectedSeats.length >= 5) {
                    Swal.fire({ icon: 'warning', title: 'Chỉ được chọn tối đa 5 ghế!', confirmButtonText: 'OK' });
                    return;
                }
                // Chọn -> Chuyển màu xanh đặc (GreenBus)
                selectedSeats.push(seatCode);
                btn.className = "seat-btn w-10 h-10 rounded-lg text-xs font-bold transition-all duration-200 flex items-center justify-center bg-green-500 text-white border-2 border-green-500 shadow-md transform scale-105";
            }
            updatePriceInfo();
        }

        function updatePriceInfo() {
            // Cập nhật giao diện tổng tiền
            const totalPrice = selectedSeats.length * TICKET_PRICE;
            
            const els = {
                seatCount: document.getElementById('seatCount'),
                selectedSeats: document.getElementById('selectedSeats'),
                totalPrice: document.getElementById('totalPrice'),
                ticketPrice: document.getElementById('ticketPrice'),
                grandTotal: document.getElementById('grandTotal')
            };

            if(els.seatCount) els.seatCount.textContent = `${selectedSeats.length} Ghế`;
            if(els.selectedSeats) els.selectedSeats.textContent = selectedSeats.length > 0 ? selectedSeats.join(', ') : 'Chưa chọn';
            
            const moneyStr = totalPrice.toLocaleString('vi-VN') + 'đ';
            if(els.totalPrice) els.totalPrice.textContent = moneyStr;
            if(els.grandTotal) els.grandTotal.textContent = moneyStr;
            if(els.ticketPrice) els.ticketPrice.textContent = TICKET_PRICE.toLocaleString('vi-VN') + 'đ';
        }

        // ==========================================
        // 5. XỬ LÝ THANH TOÁN (PAYMENT)
        // ==========================================
        function handlePayment() {
            const btnPay = document.getElementById('btnPay');
            if(!btnPay) return;

            btnPay.addEventListener("click", async function () {
                // Validate dữ liệu
                const customerName = document.querySelector('[name="customerName"]')?.value || "";
                const customerPhone = document.querySelector('[name="customerPhone"]')?.value || "";
                const email = document.querySelector('[name="email"]')?.value || "";

                if (selectedSeats.length === 0) {
                    Swal.fire({ icon: 'warning', title: 'Vui lòng chọn ít nhất 1 ghế!' });
                    return;
                }
                if (!customerName || !customerPhone || !email) {
                    Swal.fire({ icon: 'warning', title: 'Vui lòng điền đầy đủ thông tin khách hàng!' });
                    return;
                }

                // Lưu Cookie để sang trang thankyou xử lý
                const grandTotal = selectedSeats.length * TICKET_PRICE;
                document.cookie = `tripId=${tripId}; path=/`;
                document.cookie = `selectedSeats=${selectedSeats.join(' ')}; path=/`;
                document.cookie = `grandTotal=${grandTotal}; path=/`;
                document.cookie = `customerName=${encodeURIComponent(customerName)}; path=/`;
                document.cookie = `customerPhone=${encodeURIComponent(customerPhone)}; path=/`;
                document.cookie = `email=${encodeURIComponent(email)}; path=/`;

                // Gọi API giữ chỗ (Pre-booking)
                try {
                    const preResp = await fetch('/api/seats/prebooking-seat', { method: 'POST', credentials: 'include' });
                    if (!preResp.ok) throw new Error("Giữ chỗ thất bại");

                    // Chuyển hướng thanh toán
                    const paymentMethodInput = document.querySelector('input[name="payment"]:checked');
                    if (paymentMethodInput && (paymentMethodInput.value === "VNPay" || paymentMethodInput.id === "vnpay")) {
                        window.location.href = '/vnpay';
                    } else {
                        // Mặc định ZaloPay
                        processZaloPayment(customerName, grandTotal);
                    }
                } catch (e) {
                    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không thể giữ chỗ. Ghế có thể vừa bị người khác đặt.' });
                }
            });
        }

        async function processZaloPayment(fullName, amount) {
            try {
                const res = await fetch("/payment/zalo-payment", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ 
                        appUser: fullName, 
                        amount: amount, 
                        description: `Thanh toan ve xe GreenBus ${Date.now()}` 
                    })
                });
                const data = await res.json();
                if (data.returnCode === 1) {
                    window.location.href = data.orderUrl;
                } else {
                    Swal.fire({ icon: 'error', title: 'ZaloPay Error', text: data.returnMessage });
                }
            } catch (e) {
                console.error(e);
                Swal.fire({ icon: 'error', title: 'Lỗi kết nối thanh toán' });
            }
        }
    });
})();