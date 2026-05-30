(function () {
    document.addEventListener("DOMContentLoaded", function () {
        loadSelectData();
        bindSearchForm();
        bindOptionalUi();
    });

    async function loadSelectData() {
        try {
            const response = await fetch("/api/routes/get-all", {
                method: "GET",
                headers: { "Accept": "application/json" }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const data = await response.json();
            const routes = Array.isArray(data) ? data : (data.list || []);
            fillRouteSelects(routes);
        } catch (error) {
            console.error("Lỗi tải danh sách tuyến đường:", error);
        }
    }

    function fillRouteSelects(routes) {
        const routeDeparture = document.querySelector(".selectFill.departure");
        const routeArrival = document.querySelector(".selectFill.arrival");

        if (!routeDeparture || !routeArrival) {
            return;
        }

        const departures = [...new Set(routes.map(route => route.departureLocation).filter(Boolean))];
        const arrivals = [...new Set(routes.map(route => route.arrivalLocation).filter(Boolean))];

        routeDeparture.innerHTML = '<option value="">Chọn điểm đi</option>';
        routeArrival.innerHTML = '<option value="">Chọn điểm đến</option>';

        departures.forEach(location => {
            routeDeparture.appendChild(createOption(location));
        });

        arrivals.forEach(location => {
            routeArrival.appendChild(createOption(location));
        });
    }

    function createOption(value) {
        const option = document.createElement("option");
        option.value = value;
        option.textContent = value;
        return option;
    }

    function bindSearchForm() {
        const searchForm = document.getElementById("formSearchTrip");
        const dateInput = document.getElementById("departureDate");

        if (dateInput) {
            const now = new Date();
            const year = now.getFullYear();
            const month = String(now.getMonth() + 1).padStart(2, "0");
            const day = String(now.getDate()).padStart(2, "0");
            const hours = String(now.getHours()).padStart(2, "0");
            const minutes = String(now.getMinutes()).padStart(2, "0");
            dateInput.setAttribute("min", `${year}-${month}-${day}T${hours}:${minutes}`);
        }

        if (!searchForm) {
            return;
        }

        searchForm.addEventListener("submit", function (event) {
            const departureVal = document.getElementById("departure")?.value;
            const arrivalVal = document.getElementById("arrival")?.value;
            const selectedDate = dateInput?.value ? new Date(dateInput.value) : null;

            if (!departureVal || !arrivalVal || !dateInput?.value) {
                event.preventDefault();
                Swal.fire({
                    icon: "warning",
                    title: "Thiếu thông tin",
                    text: "Vui lòng chọn đầy đủ điểm đi, điểm đến và ngày đi.",
                    confirmButtonColor: "#10b981"
                });
                return;
            }

            if (selectedDate < new Date()) {
                event.preventDefault();
                Swal.fire({
                    icon: "error",
                    title: "Ngày không hợp lệ",
                    text: "Vui lòng chọn ngày hiện tại hoặc tương lai.",
                    confirmButtonColor: "#10b981"
                });
            }
        });
    }

    function bindOptionalUi() {
        const mobileMenuBtn = document.getElementById("mobileMenuBtn");
        const mobileMenu = document.getElementById("mobileMenu");
        if (mobileMenuBtn && mobileMenu) {
            mobileMenuBtn.addEventListener("click", () => mobileMenu.classList.toggle("hidden"));
        }

        const loginBtn = document.getElementById("loginBtn");
        const loginModal = document.getElementById("loginModal");
        const closeLoginModal = document.getElementById("closeLoginModal");
        if (loginBtn && loginModal) {
            loginBtn.addEventListener("click", () => {
                loginModal.classList.remove("hidden");
                loginModal.classList.add("flex");
            });
        }
        if (closeLoginModal && loginModal) {
            closeLoginModal.addEventListener("click", () => closeModal(loginModal));
            loginModal.addEventListener("click", event => {
                if (event.target === loginModal) {
                    closeModal(loginModal);
                }
            });
        }

        const carousel = document.querySelector(".carousel");
        const carouselItems = document.querySelectorAll(".carousel-item");
        const carouselButtons = document.querySelectorAll(".carousel + div button");
        let currentSlide = 0;

        if (carousel && carouselItems.length && carouselButtons.length) {
            const updateCarousel = () => {
                carousel.style.transform = `translateX(-${currentSlide * 100}%)`;
                carouselButtons.forEach((button, index) => {
                    button.classList.toggle("bg-white", index === currentSlide);
                    button.classList.toggle("bg-gray-300", index !== currentSlide);
                });
            };

            carouselButtons.forEach((button, index) => {
                button.addEventListener("click", () => {
                    currentSlide = index;
                    updateCarousel();
                });
            });

            setInterval(() => {
                currentSlide = (currentSlide + 1) % carouselItems.length;
                updateCarousel();
            }, 5000);
        }

        const dateOnlyInput = document.querySelector('input[type="date"]');
        if (dateOnlyInput) {
            dateOnlyInput.min = new Date().toISOString().split("T")[0];
        }

        if ("IntersectionObserver" in window) {
            const observer = new IntersectionObserver((entries, activeObserver) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.classList.add("animate-slide-in");
                        activeObserver.unobserve(entry.target);
                    }
                });
            }, { threshold: 0.1 });

            document.querySelectorAll(".grid > div").forEach(element => observer.observe(element));
        }
    }

    function closeModal(modal) {
        modal.classList.remove("flex");
        modal.classList.add("hidden");
    }
})();
