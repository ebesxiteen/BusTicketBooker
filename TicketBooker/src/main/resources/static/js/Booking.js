(function () {
    document.addEventListener("DOMContentLoaded", function () {
        // Biến toàn cục
        let TICKET_PRICE = 0;
        let selectedSeats = [];
        const MAX_SEATS = 5;
        let BUS_CAPACITY = 36; // mặc định

        init();

        function init() {
            try {
                const urlParams = new URLSearchParams(window.location.search);
                const tripId = urlParams.get('tripId');
                if (!tripId) {
                    console.error("Không tìm thấy tripId trên URL");
                    return;
                }

                // Lấy info chuyến + vẽ sơ đồ ghế + check ghế đã đặt
                fetchTripDetails(tripId);

                // Khởi tạo thanh toán
                handlePayment();
            } catch (e) {
                console.error("Lỗi trong init(): ", e);
            }
        }

        // ============================================================
        // 1. LẤY CHI TIẾT CHUYẾN XE
        // ============================================================
        async function fetchTripDetails(tripId) {
            try {
                const response = await fetch(`/api/trips/${tripId}`);

                if (!response.ok) {
                    console.error("Lỗi gọi API /api/trips: ", response.status);
                    return;
                }

                const data = await response.json();

                // Cập nhật UI
                const depLoc = document.getElementById('departureLocation');
                const depTime = document.getElementById('departureTime');
                const ticketPriceUi = document.getElementById('ticketPrice');

                if (depLoc) {
                    depLoc.textContent = `${data.departureLocation} ➝ ${data.arrivalLocation}`;
                }
                if (depTime) {
                    depTime.textContent = data.departureTime;
                }

                // Lưu giá vé
                let rawPrice = data.totalPrice
                    ? data.totalPrice.toString().replace(/[^0-9]/g, '')
                    : "0";
                TICKET_PRICE = parseInt(rawPrice || "0", 10);

                if (ticketPriceUi) {
                    ticketPriceUi.textContent = formatMoney(TICKET_PRICE);
                }

                // Lấy capacity nếu backend có trả, không có thì giữ mặc định
                if (data.capacity && !isNaN(parseInt(data.capacity))) {
                    BUS_CAPACITY = parseInt(data.capacity, 10);
                }

                // Vẽ sơ đồ ghế theo capacity
                renderSeatMap(BUS_CAPACITY);

                // Check ghế đã đặt
                fetchBookedSeats(tripId);

                // Cập nhật giá lần đầu
                updatePriceInfo();

            } catch (error) {
                console.error('Lỗi lấy thông tin chuyến:', error);
            }
        }

        // ============================================================
        // 2. VẼ SƠ ĐỒ GHẾ
        // ============================================================
        function renderSeatMap(capacity) {
            const container = document.getElementById('seatMapContainer');
            if (!container) return;

            container.innerHTML = '';

            // Chia ghế cho 2 tầng: A (dưới), B (trên)
            const floor1Count = Math.ceil(capacity / 2);
            const floor2Count = capacity - floor1Count;

            const floor1 = createFloor('Tầng dưới', 'A', floor1Count);
            const floor2 = createFloor('Tầng trên', 'B', floor2Count);

            container.appendChild(floor1);
            container.appendChild(floor2);
        }

        function createFloor(title, prefix, count) {
            const floorDiv = document.createElement('div');
            floorDiv.className = 'floor-container bg-gray-100 p-4 rounded-lg mx-2';

            const titleEl = document.createElement('h3');
            titleEl.className = 'text-center font-bold text-gray-500 mb-3';
            titleEl.innerText = title;
            floorDiv.appendChild(titleEl);

            const gridDiv = document.createElement('div');
            gridDiv.className = 'grid grid-cols-3 gap-3';

            for (let i = 1; i <= count; i++) {
                const seatCode = `${prefix}${i.toString().padStart(2, '0')}`;

                const btn = document.createElement('button');
                btn.className =
                    'seat seat-available w-10 h-10 md:w-12 md:h-12 rounded-lg border ' +
                    'border-gray-300 flex items-center justify-center text-xs font-semibold ' +
                    'shadow-sm bg-white hover:border-emerald-500 transition relative';
                btn.innerText = seatCode;
                btn.dataset.code = seatCode;

                btn.addEventListener('click', () => toggleSeat(btn, seatCode));

                gridDiv.appendChild(btn);
            }

            floorDiv.appendChild(gridDiv);
            return floorDiv;
        }

        // ============================================================
        // 3. GHẾ ĐÃ ĐẶT
        // ============================================================
        async function fetchBookedSeats(tripId) {
            try {
                const response = await fetch(`/api/seats/${tripId}/booked`);
                if (!response.ok) {
                    console.error("Lỗi API /api/seats/{tripId}/booked: ", response.status);
                    return;
                }
                const bookedSeats = await response.json();
                markBookedSeats(bookedSeats);
            } catch (error) {
                console.error('Lỗi check ghế đã đặt:', error);
            }
        }

        function markBookedSeats(bookedList) {
            const allSeats = document.querySelectorAll('.seat');
            allSeats.forEach(btn => {
                const code = btn.dataset.code;
                if (bookedList.includes(code)) {
                    btn.classList.remove('seat-available', 'bg-white');
                    btn.classList.add(
                        'seat-booked',
                        'bg-gray-300',
                        'text-gray-500',
                        'cursor-not-allowed'
                    );
                    btn.disabled = true;
                    btn.title = "Ghế đã có người đặt";
                }
            });
        }

        // ============================================================
        // 4. CHỌN GHẾ + TÍNH TIỀN
        // ============================================================
        function toggleSeat(btn, code) {
            if (btn.disabled) return;

            if (selectedSeats.includes(code)) {
                selectedSeats = selectedSeats.filter(s => s !== code);
                btn.classList.remove('bg-emerald-600', 'text-white', 'border-emerald-600');
                btn.classList.add('bg-white', 'text-gray-800');
            } else {
                if (selectedSeats.length >= MAX_SEATS) {
                    Swal.fire({
                        icon: 'warning',
                        title: 'Giới hạn',
                        text: `Chỉ được chọn tối đa ${MAX_SEATS} ghế.`
                    });
                    return;
                }
                selectedSeats.push(code);
                btn.classList.remove('bg-white', 'text-gray-800');
                btn.classList.add('bg-emerald-600', 'text-white', 'border-emerald-600');
            }
            updatePriceInfo();
        }

        function updatePriceInfo() {
            const countEl = document.getElementById('seatCount');
            const listEl = document.getElementById('selectedSeats');
            const totalEl = document.getElementById('totalPrice');
            const grandEl = document.getElementById('grandTotal');

            if (countEl) countEl.innerText = `${selectedSeats.length} Ghế`;
            if (listEl) listEl.innerText =
                selectedSeats.length > 0 ? selectedSeats.join(', ') : 'Chưa chọn';

            const total = selectedSeats.length * TICKET_PRICE;
            const formattedTotal = formatMoney(total);

            if (totalEl) totalEl.innerText = formattedTotal;
            if (grandEl) grandEl.innerText = formattedTotal;
        }

        // ============================================================
        // 5. XỬ LÝ THANH TOÁN
        // ============================================================
        function handlePayment() {
            const btnPay = document.getElementById('btnPay');
            if (!btnPay) return;

            btnPay.addEventListener('click', async () => {
                const name = document.querySelector('input[name="customerName"]').value;
                const phone = document.querySelector('input[name="customerPhone"]').value;
                const email = document.querySelector('input[name="email"]').value;

                if (selectedSeats.length === 0) {
                    Swal.fire({
                        icon: 'warning',
                        title: 'Chưa chọn ghế',
                        text: 'Vui lòng chọn ít nhất 1 ghế!'
                    });
                    return;
                }
                if (!name || !phone || !email) {
                    Swal.fire({
                        icon: 'warning',
                        title: 'Thiếu thông tin',
                        text: 'Vui lòng điền đầy đủ thông tin khách hàng!'
                    });
                    return;
                }

                const tripId = new URLSearchParams(window.location.search).get('tripId');
                const total = selectedSeats.length * TICKET_PRICE;

                setCookie("tripId", tripId, 1);
                setCookie("selectedSeats", selectedSeats.join(','), 1);
                setCookie("grandTotal", total, 1);
                setCookie("customerName", name, 1);
                setCookie("customerPhone", phone, 1);
                setCookie("email", email, 1);

                try {
                    await fetch('/api/seats/prebooking-seat', { method: 'POST' });

                    const paymentMethod =
                        document.querySelector('input[name="payment"]:checked').value;

                    // Tạm thời vẫn redirect thẳng tới thankyou
                    window.location.href = `/greenbus/thankyou?paymentStatus=1`;

                } catch (e) {
                    console.error(e);
                    Swal.fire({
                        icon: 'error',
                        title: 'Lỗi',
                        text: 'Không thể xử lý đặt vé lúc này'
                    });
                }
            });
        }

        // Helpers
        function formatMoney(amount) {
            return new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(amount || 0);
        }

        function setCookie(cname, cvalue, exhours) {
            const d = new Date();
            d.setTime(d.getTime() + (exhours * 60 * 60 * 1000));
            const expires = "expires=" + d.toUTCString();
            document.cookie = cname + "=" + encodeURIComponent(cvalue) + ";" + expires + ";path=/";
        }
    });
})();
