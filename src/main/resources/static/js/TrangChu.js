(function () {
    document.addEventListener("DOMContentLoaded", function () {
        loadSelectData();

        function loadSelectData() {
            getRoutes();

            function getRoutes() {
                // Đảm bảo URL này được public trong SecurityConfig
                const fetchUrl = "/api/routes/get-routes"; 
                fetch(fetchUrl, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" }
                }).then(response => {
                    if (response.ok) {
                        return response.json();
                    } else {
                        throw new Error(`HTTP error! Status: ${response.status}`);
                    }
                }).then(data => {
                    if (data && data.list) {
                        preSetSelect(data.list);
                    }
                }).catch(e => console.log("Lỗi tải tuyến đường:", e));
            }

            // ============================================================
        // 2. LOGIC SUBMIT FORM & KIỂM TRA NGÀY (Validation) - MỚI THÊM
        // ============================================================
        const searchForm = document.getElementById("formSearchTrip");
        
        if (searchForm) {
            searchForm.addEventListener("submit", function(event) {
                // Lấy giá trị ngày từ input
                const dateInput = document.getElementById("departureDate");
                const departureVal = document.getElementById("departure").value;
                const arrivalVal = document.getElementById("arrival").value;

                // Kiểm tra xem đã chọn đủ thông tin chưa
                if (!departureVal || !arrivalVal || !dateInput.value) {
                    event.preventDefault(); // Chặn submit
                    Swal.fire({
                        icon: 'warning',
                        title: 'Thiếu thông tin',
                        text: 'Vui lòng chọn đầy đủ Điểm đi, Điểm đến và Ngày đi!',
                        confirmButtonColor: '#10b981'
                    });
                    return;
                }

                // --- KIỂM TRA NGÀY QUÁ KHỨ ---
                const selectedDate = new Date(dateInput.value);
                const now = new Date();
                
                // Reset giờ/phút/giây về 0 để so sánh ngày thôi (nếu muốn so sánh chính xác cả giờ thì bỏ đoạn này)
                // Ở đây mình so sánh cả giờ để chặt chẽ hơn (không đặt vé cho giờ đã qua trong ngày hôm nay)
                // Tuy nhiên, để đơn giản cho người dùng, thường chỉ so sánh ngày.
                // Dưới đây là logic so sánh cả ngày và giờ:
                
                // Nếu ngày chọn nhỏ hơn thời điểm hiện tại
                if (selectedDate < now) {
                    event.preventDefault(); // Chặn submit
                    Swal.fire({
                        icon: 'error',
                        title: 'Ngày không hợp lệ',
                        text: 'Không tìm thấy chuyến xe nào trong quá khứ. Vui lòng chọn ngày hiện tại hoặc tương lai!',
                        confirmButtonColor: '#10b981'
                    });
                    return;
                }

                // Nếu mọi thứ OK, form sẽ tự động submit và chuyển sang trang /greenbus/search-trips
            });
        }

        // Tự động set min date cho input ngày để người dùng không chọn được ngày cũ trên lịch
        const dateInput = document.getElementById("departureDate");
        if (dateInput) {
            const now = new Date();
            // Format YYYY-MM-DDTHH:mm để set min cho input datetime-local
            const year = now.getFullYear();
            const month = String(now.getMonth() + 1).padStart(2, '0');
            const day = String(now.getDate()).padStart(2, '0');
            const hours = String(now.getHours()).padStart(2, '0');
            const minutes = String(now.getMinutes()).padStart(2, '0');
            
            const minDateTime = `${year}-${month}-${day}T${hours}:${minutes}`;
            dateInput.setAttribute("min", minDateTime);
        }

            function preSetSelect(data) {
                const routeDeparture = document.querySelector(".selectFill.departure");
                const routeArrival = document.querySelector(".selectFill.arrival");
                
                // Kiểm tra null trước khi thao tác
                if (!routeDeparture || !routeArrival) return;

                routeDeparture.innerHTML = "";
                routeArrival.innerHTML = "";
                
                let departureNames = [];
                let arrivalNames = [];
                
                data.forEach(route => {
                    departureNames.push(route.departureLocation);
                    arrivalNames.push(route.arrivalLocation);
                });
                
                // Dùng Set để lọc trùng
                const listDepartures = [...new Set(departureNames)];
                const listArrivals = [...new Set(arrivalNames)];
                
                listDepartures.forEach(loc => {
                    routeDeparture.innerHTML += `<option value="${loc}">${loc}</option>`;
                });
                
                listArrivals.forEach(loc => {
                    routeArrival.innerHTML += `<option value="${loc}">${loc}</option>`;
                });
            }
        }
    });
})();

// Mobile menu toggle
const mobileMenuBtn = document.getElementById('mobileMenuBtn');
const mobileMenu = document.getElementById('mobileMenu');
mobileMenuBtn.addEventListener('click', () => {
    mobileMenu.classList.toggle('hidden');
});

// Login modal
const loginBtn = document.getElementById('loginBtn');
const loginModal = document.getElementById('loginModal');
const closeLoginModal = document.getElementById('closeLoginModal');

loginBtn.addEventListener('click', () => {
    loginModal.classList.remove('hidden');
    loginModal.classList.add('flex');
});

closeLoginModal.addEventListener('click', () => {
    loginModal.classList.remove('flex');
    loginModal.classList.add('hidden');
});

// Close modal when clicking outside
loginModal.addEventListener('click', (e) => {
    if (e.target === loginModal) {
        loginModal.classList.remove('flex');
        loginModal.classList.add('hidden');
    }
});

// Carousel functionality
const carousel = document.querySelector('.carousel');
const carouselItems = document.querySelectorAll('.carousel-item');
const carouselButtons = document.querySelectorAll('.carousel + div button');
let currentSlide = 0;

function updateCarousel() {
    carousel.style.transform = `translateX(-${currentSlide * 100}%)`;
    carouselButtons.forEach((button, index) => {
        button.classList.toggle('bg-white', index === currentSlide);
        button.classList.toggle('bg-gray-300', index !== currentSlide);
    });
}

carouselButtons.forEach((button, index) => {
    button.addEventListener('click', () => {
        currentSlide = index;
        updateCarousel();
    });
});

setInterval(() => {
    currentSlide = (currentSlide + 1) % carouselItems.length;
    updateCarousel();
}, 5000);

// Date input min date
const dateInput = document.querySelector('input[type="date"]');
dateInput.min = new Date().toISOString().split('T')[0];

// Animate elements on scroll
const animateOnScroll = (entries, observer) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('animate-slide-in');
            observer.unobserve(entry.target);
        }
    });
};

const observer = new IntersectionObserver(animateOnScroll, {
    root: null,
    threshold: 0.1
});

document.querySelectorAll('.grid > div').forEach(el => observer.observe(el));

