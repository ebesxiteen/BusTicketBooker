(function () {
    document.addEventListener("DOMContentLoaded", function () {
        console.log("🚀 FindTrip.js Loaded - V4 Clean Fix!");

        // Biến toàn cục quản lý trạng thái
        let tripsData = []; // Lưu trữ danh sách gốc nếu cần (hiện tại dùng DOM)
        
        // DOM Elements
        const rangeMin = document.getElementById("range-min");
        const rangeMax = document.getElementById("range-max");
        const track = document.querySelector(".slider-track");
        const priceMinDisplay = document.getElementById("price-min-display");
        const priceMaxDisplay = document.getElementById("price-max-display");
        
        // Khởi chạy
        init();

        function init() {
            syncUrlParamsToForm();
            loadSelectData(); // Load dropdown
            
            // Đợi 1 chút để DOM ổn định rồi mới init các tính năng lọc
            setTimeout(() => {
                initPriceSlider();
                initFilters();
                // Chạy lọc lần đầu để khớp với trạng thái mặc định
                filterTrips(); 
            }, 100);
        }

        // ============================================================
        // 1. SYNC URL (Giữ nguyên logic chuẩn)
        // ============================================================
        function syncUrlParamsToForm() {
            const params = new URLSearchParams(window.location.search);
            const arrival = params.get('arrival');
            const departure = params.get('departure');
            const dateVal = params.get('date');

            const dateInput = document.getElementById("departureDate");
            if (dateInput) {
                if (dateVal) dateInput.value = dateVal.includes("T") ? dateVal : (dateVal + "T00:00");
                else {
                    // Set default today
                    const now = new Date();
                    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
                    dateInput.value = now.toISOString().slice(0, 16);
                }
            }

            const safeDecode = (s) => { try { return decodeURIComponent(s || ''); } catch(e){ return s; } };

            if(arrival) {
                const el = document.getElementById("arrival");
                if(el) { el.value = safeDecode(arrival); el.setAttribute('data-selected', safeDecode(arrival)); }
            }
            if(departure) {
                const el = document.getElementById("departure");
                if(el) { el.value = safeDecode(departure); el.setAttribute('data-selected', safeDecode(departure)); }
            }
        }

        // ============================================================
        // 2. LOAD DROPDOWN (Giữ nguyên)
        // ============================================================
        function loadSelectData() {
            fetch("/api/routes/get-all", { method: "GET", headers: { "Content-Type": "application/json" } })
            .then(res => res.json())
            .then(data => {
                const list = Array.isArray(data) ? data : (data.list || []);
                const depEl = document.getElementById("departure");
                const arrEl = document.getElementById("arrival");
                
                if(!depEl || !arrEl) return;

                const selDep = depEl.getAttribute('data-selected');
                const selArr = arrEl.getAttribute('data-selected');

                depEl.innerHTML = '<option value="">Chọn điểm đi</option>';
                arrEl.innerHTML = '<option value="">Chọn điểm đến</option>';

                const deps = new Set(), arrs = new Set();
                list.forEach(i => {
                    const d = i.departureLocation || i.route?.departureLocation;
                    const a = i.arrivalLocation || i.route?.arrivalLocation;
                    if(d) deps.add(d);
                    if(a) arrs.add(a);
                });

                deps.forEach(v => depEl.add(new Option(v, v, false, v == selDep)));
                arrs.forEach(v => arrEl.add(new Option(v, v, false, v == selArr)));
            }).catch(console.warn);
        }

        // ============================================================
        // 3. SLIDER GIÁ (Logic Đơn Giản & Hiệu Quả)
        // ============================================================
        let minGap = 50000;
        let sliderMaxVal = 2000000; // Mặc định

        function initPriceSlider() {
            if (!rangeMin || !rangeMax) return;

            // 1. Tìm giá Max thực tế từ DOM
            const items = document.querySelectorAll('.trip-item');
            let prices = [];
            items.forEach(el => {
                let p = parseFloat(el.getAttribute('data-price'));
                if(!isNaN(p)) prices.push(p);
            });

            if (prices.length > 0) {
                let maxP = Math.max(...prices);
                sliderMaxVal = Math.ceil(maxP / 100000) * 100000;
                if(sliderMaxVal < 500000) sliderMaxVal = 500000;
            }

            // 2. Set Attributes
            rangeMin.max = sliderMaxVal;
            rangeMax.max = sliderMaxVal;
            rangeMin.value = 0;
            rangeMax.value = sliderMaxVal;

            // 3. Update UI lần đầu
            updateSliderUI();

            // 4. Gắn sự kiện (Dùng oninput trực tiếp)
            rangeMin.oninput = function() {
                let minVal = parseInt(rangeMin.value);
                let maxVal = parseInt(rangeMax.value);

                if (maxVal - minVal < minGap) {
                    rangeMin.value = maxVal - minGap;
                }
                updateSliderUI();
                filterTrips(); // Gọi lọc ngay khi kéo
            };

            rangeMax.oninput = function() {
                let minVal = parseInt(rangeMin.value);
                let maxVal = parseInt(rangeMax.value);

                if (maxVal - minVal < minGap) {
                    rangeMax.value = minVal + minGap;
                }
                updateSliderUI();
                filterTrips(); // Gọi lọc ngay khi kéo
            };
        }

        function updateSliderUI() {
            let minVal = parseInt(rangeMin.value);
            let maxVal = parseInt(rangeMax.value);
            
            // Cập nhật số tiền hiển thị
            if(priceMinDisplay) priceMinDisplay.innerText = formatMoney(minVal);
            if(priceMaxDisplay) priceMaxDisplay.innerText = formatMoney(maxVal);

            // Tô màu thanh track
            let percent1 = (minVal / sliderMaxVal) * 100;
            let percent2 = (maxVal / sliderMaxVal) * 100;
            
            if(track) {
                track.style.background = `linear-gradient(to right, #e5e7eb ${percent1}%, #10b981 ${percent1}%, #10b981 ${percent2}%, #e5e7eb ${percent2}%)`;
            }
        }

        // ============================================================
        // 4. BỘ LỌC TỔNG HỢP
        // ============================================================
        let currentBusType = 'ALL';

        function initFilters() {
            // Checkbox Giờ
            document.querySelectorAll('input[name="timeFilter"]').forEach(cb => {
                cb.addEventListener('change', filterTrips);
            });

            // Button Loại Xe
            document.querySelectorAll('.btn-type-filter').forEach(btn => {
                btn.addEventListener('click', function() {
                    let type = this.getAttribute('data-value');
                    // Toggle logic
                    currentBusType = (currentBusType === type) ? 'ALL' : type;
                    updateTypeButtonsUI();
                    filterTrips();
                });
            });

            // Nút Reset
            const resetBtn = document.querySelector('.btn-reset-filter');
            if(resetBtn) {
                resetBtn.addEventListener('click', resetAllFilters);
            }
            
            // Format tiền cho danh sách (chỉ chạy 1 lần để đẹp)
            document.querySelectorAll('.trip-price').forEach(el => {
                // Kiểm tra nếu chưa format thì mới làm
                if(!el.innerText.includes('₫') && !el.innerText.includes('đ')) {
                    let v = parseFloat(el.innerText.replace(/[^0-9]/g, ''));
                    if(!isNaN(v)) el.innerText = formatMoney(v);
                }
            });
        }

        function filterTrips() {
            const items = document.querySelectorAll('.trip-item');
            
            // Lấy điều kiện Giờ
            const timeChecked = Array.from(document.querySelectorAll('input[name="timeFilter"]:checked')).map(c => c.value);
            
            // Lấy điều kiện Giá
            let pMin = rangeMin ? parseInt(rangeMin.value) : 0;
            let pMax = rangeMax ? parseInt(rangeMax.value) : 999999999;

            let count = 0;

            items.forEach(item => {
                // Lấy data từ attribute
                let hour = parseInt(item.getAttribute('data-hour'));
                let type = normalizeType(item.getAttribute('data-type'));
                let price = parseFloat(item.getAttribute('data-price'));

                // 1. Check Giờ
                let timeOk = (timeChecked.length === 0); // Nếu không check cái nào thì mặc định true
                if (!timeOk) {
                    timeChecked.forEach(range => {
                        let [start, end] = range.split('-').map(Number);
                        if (hour >= start && hour < end) timeOk = true;
                    });
                }

                // 2. Check Loại Xe
                let typeOk = (currentBusType === 'ALL' || type === normalizeType(currentBusType));

                // 3. Check Giá
                let priceOk = (price >= pMin && price <= pMax);

                // KẾT QUẢ
                if (timeOk && typeOk && priceOk) {
                    item.classList.remove('hidden');
                    count++;
                } else {
                    item.classList.add('hidden');
                }
            });

            // Cập nhật số lượng
            const counter = document.getElementById('tripCountDisplay');
            if(counter) counter.innerText = `(${count} chuyến)`;

            // Hiển thị thông báo rỗng
            const noMsg = document.getElementById('noResultFilter');
            if(noMsg) {
                if(count === 0 && items.length > 0) noMsg.classList.remove('hidden');
                else noMsg.classList.add('hidden');
            }
        }

        function resetAllFilters() {
            // Reset Giờ
            document.querySelectorAll('input[name="timeFilter"]').forEach(c => c.checked = false);
            
            // Reset Loại Xe
            currentBusType = 'ALL';
            updateTypeButtonsUI();

            // Reset Giá
            if(rangeMin && rangeMax) {
                rangeMin.value = 0;
                rangeMax.value = rangeMax.max;
                updateSliderUI();
            }

            filterTrips();
        }

        function updateTypeButtonsUI() {
            document.querySelectorAll('.btn-type-filter').forEach(btn => {
                let isActive = btn.getAttribute('data-value') === currentBusType;
                if (isActive) {
                    btn.classList.remove('bg-gray-50', 'text-gray-700', 'border-gray-300');
                    btn.classList.add('bg-emerald-600', 'text-white', 'border-transparent');
                } else {
                    btn.classList.add('bg-gray-50', 'text-gray-700', 'border-gray-300');
                    btn.classList.remove('bg-emerald-600', 'text-white', 'border-transparent');
                }
            });
        }

        // --- Helpers ---
        function normalizeType(s) {
            if(!s) return "BED"; 
            s = s.toUpperCase();
            if(s.includes("BED") || s.includes("GIUONG") || s.includes("LIMOUSINE")) return "BED";
            if(s.includes("SEAT") || s.includes("GHE")) return "SEAT";
            return "BED";
        }

        function formatMoney(n) {
            return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n);
        }
    });
})();