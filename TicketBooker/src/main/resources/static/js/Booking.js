(function () {
    document.addEventListener("DOMContentLoaded", function () {
        // Khởi tạo các chức năng
        booking();
        handlePayment();
        fetchTripDetails();

        let TICKET_PRICE = 0; // Giá vé
        let tripTotalPrice = 0;

        // 1. Lấy thông tin chuyến xe
        async function fetchTripDetails() {
            try {
                const urlParams = new URLSearchParams(window.location.search);
                const tripId = urlParams.get('tripId');

                const response = await fetch(`/admin/trips/${tripId}`);
                if (response.ok) {
                    const data = await response.json();
                    document.getElementById('departureLocation').textContent = `${data.departureLocation} - ${data.arrivalLocation}`;
                    document.getElementById('departureTime').textContent = data.departureTime;
                    
                    console.log('Total Price from API:', data.totalPrice);
                    TICKET_PRICE = data.totalPrice;
                    updatePriceInfo();
                } else {
                    console.error('Không thể lấy thông tin chuyến xe');
                }
            } catch (error) {
                console.error('Lỗi khi lấy thông tin chuyến xe:', error);
            }
        }

        // 2. Logic chọn ghế
        function booking() {
            let selectedSeats = [];
            const seats = document.querySelectorAll('.seat');
            const urlParams = new URLSearchParams(window.location.search);
            const tripId = urlParams.get('tripId');

            async function fetchBookedSeats() {
                try {
                    const response = await fetch(`/api/seats/${tripId}/booked`);
                    if (response.ok) {
                        const bookedSeats = await response.json();
                        markBookedSeats(bookedSeats);
                    }
                } catch (error) {
                    console.error('Lỗi khi lấy danh sách ghế đã đặt:', error);
                }
            }

            function markBookedSeats(bookedSeats) {
                seats.forEach(seat => {
                    const seatCode = seat.textContent.trim();
                    if (bookedSeats.includes(seatCode)) {
                        seat.classList.add('seat-booked', 'bg-gray-300', 'cursor-not-allowed'); // Thêm class booked
                        seat.classList.remove('seat-available');
                        seat.disabled = true;
                    }
                });
            }

            // Global scope function để gọi từ bên ngoài nếu cần
            window.updatePriceInfo = function() {
                const seatCountEl = document.getElementById('seatCount');
                const selectedSeatsEl = document.getElementById('selectedSeats');
                const totalPriceEl = document.getElementById('totalPrice');
                const ticketPriceEl = document.getElementById('ticketPrice');
                const grandTotalEl = document.getElementById('grandTotal');

                if(seatCountEl) seatCountEl.textContent = `${selectedSeats.length} Ghế`;
                if(selectedSeatsEl) selectedSeatsEl.textContent = selectedSeats.join(', ');
                
                const totalPrice = selectedSeats.length * TICKET_PRICE;
                if(totalPriceEl) totalPriceEl.textContent = `${totalPrice.toLocaleString()}đ`;
                if(ticketPriceEl) ticketPriceEl.textContent = `${TICKET_PRICE.toLocaleString()}đ`;
                if(grandTotalEl) grandTotalEl.textContent = `${totalPrice.toLocaleString()}đ`;
            }

            function toggleSeat(button) {
                if (button.classList.contains('seat-booked') || button.disabled) {
                    Swal.fire({ icon: 'error', title: 'Ghế này đã được đặt!', confirmButtonText: 'OK' });
                    return;
                }

                const seatCode = button.textContent.trim();

                if (selectedSeats.includes(seatCode)) {
                    // Bỏ chọn
                    button.classList.remove('seat-selected', 'bg-green-500', 'text-white');
                    button.classList.add('seat-available');
                    selectedSeats = selectedSeats.filter(s => s !== seatCode);
                } else {
                    // Chọn mới
                    if (selectedSeats.length >= 5) {
                        Swal.fire({ icon: 'warning', title: 'Không thể chọn quá 5 ghế!', confirmButtonText: 'OK' });
                        return;
                    }
                    button.classList.remove('seat-available');
                    button.classList.add('seat-selected', 'bg-green-500', 'text-white');
                    selectedSeats.push(seatCode);
                }
                updatePriceInfo();
            }

            fetchBookedSeats();

            seats.forEach(seat => {
                seat.addEventListener('click', function () {
                    toggleSeat(seat);
                });
            });
        }

        // 3. Xử lý Thanh toán (Payment)
        function handlePayment() {
            const btnPay = document.getElementById('btnPay');
            if(!btnPay) return;

            btnPay.addEventListener("click", async function () {
                console.log("Bắt đầu xử lý thanh toán...");

                try {
                    // Lấy dữ liệu từ DOM
                    const tripId = new URLSearchParams(window.location.search).get('tripId') || "";
                    const selectedSeats = document.getElementById('selectedSeats')?.textContent.trim() || "";
                    const grandTotalRaw = document.getElementById('totalPrice')?.textContent || "0";
                    const customerName = document.querySelector('[name="customerName"]')?.value || "";
                    const customerPhone = document.querySelector('[name="customerPhone"]')?.value || "";
                    const email = document.querySelector('[name="email"]')?.value || "";

                    // Validate
                    if (!selectedSeats || !customerName || !customerPhone || !email) {
                        Swal.fire({ icon: 'warning', title: 'Vui lòng điền đầy đủ thông tin!', confirmButtonText: 'OK' });
                        return;
                    }

                    // Format giá tiền (bỏ dấu đ, dấu chấm)
                    const grandTotal = parseInt(grandTotalRaw.replace(/[^0-9]/g, ""), 10);

                    // Lưu Cookie
                    document.cookie = `tripId=${encodeURIComponent(tripId)}; path=/`;
                    document.cookie = `selectedSeats=${encodeURIComponent(selectedSeats)}; path=/`;
                    document.cookie = `grandTotal=${encodeURIComponent(grandTotal)}; path=/`;
                    document.cookie = `customerName=${encodeURIComponent(customerName)}; path=/`;
                    document.cookie = `customerPhone=${encodeURIComponent(customerPhone)}; path=/`;
                    document.cookie = `email=${encodeURIComponent(email)}; path=/`;

                    // Gọi API giữ chỗ (Prebooking)
                    const preBookingResponse = await fetch('/api/seats/prebooking-seat', {
                        method: 'POST',
                        // headers: { 'Content-Type': 'application/json' }, // Tùy backend yêu cầu
                        credentials: 'include'
                    });

                    if (!preBookingResponse.ok) {
                        Swal.fire({ icon: 'error', title: 'Đặt chỗ thất bại. Vui lòng thử lại!', confirmButtonText: 'OK' });
                        return;
                    }

                    // Kiểm tra phương thức thanh toán
                    const selectedPaymentInput = document.querySelector('input[name="payment"]:checked');
                    if (selectedPaymentInput) {
                        // Tìm thẻ span chứa text bên cạnh input (hoặc lấy value của input)
                        const paymentMethod = selectedPaymentInput.closest('label')?.querySelector('span')?.textContent.trim() || selectedPaymentInput.value;
                        console.log("Phương thức:", paymentMethod);

                        if (paymentMethod === "VNPay" || selectedPaymentInput.value === "VNPay") {
                            window.location.href = '/vnpay'; // Chuyển hướng VNPay
                        } 
                        else {
                            // Mặc định ZaloPay hoặc các loại khác
                            await processZaloPayment(fullName, grandTotal);
                        }
                    } else {
                        Swal.fire({ icon: 'warning', title: 'Vui lòng chọn phương thức thanh toán!', confirmButtonText: 'OK' });
                    }

                } catch (error) {
                    console.error("Lỗi thanh toán:", error);
                    Swal.fire({ icon: 'error', title: 'Đã xảy ra lỗi hệ thống.', text: error.message });
                }
            });
        }

        // Hàm xử lý ZaloPay tách riêng
        async function processZaloPayment(fullName, amount) {
            const description = "Thanh toan ve xe " + Date.now();
            
            try {
                // Gọi API tạo đơn hàng ZaloPay
                // LƯU Ý: Sửa URL thành đường dẫn tương đối để tự động nhận port hiện tại (8000)
                const createOrderRes = await fetch("/payment/zalo-payment", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ appUser: fullName, amount: amount, description: description })
                });

                const orderData = await createOrderRes.json();

                if (orderData.returnCode === 1) {
                    // Nếu tạo đơn thành công -> Chuyển hướng sang trang thanh toán của ZaloPay
                    window.location.href = orderData.orderUrl; 
                    
                    // (Tùy chọn) Gọi API lưu trạng thái nếu cần thiết
                    // await fetch("/payment/zalo-payment-status", { ... });
                } else {
                    Swal.fire({ icon: 'error', title: 'Lỗi ZaloPay', text: orderData.returnMessage });
                }
            } catch (e) {
                console.error("ZaloPay Error:", e);
                Swal.fire({ icon: 'error', title: 'Không thể kết nối tới ZaloPay' });
            }
        }
    });
})();