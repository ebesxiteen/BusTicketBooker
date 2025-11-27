(function () {
    document.addEventListener("DOMContentLoaded", function () {
        // Khởi tạo các controller
        modalController();
        deleteController();
        getDetailsController();
        searchController();

        // 1. Xử lý Xóa Tài xế
        function deleteController() {
            const deleteBtns = document.querySelectorAll(".delete-btn");
            
            deleteBtns.forEach(btn => {
                btn.addEventListener("click", function () {
                    const driverId = btn.dataset.id;
                    
                    if (!confirm("Bạn có chắc chắn muốn xóa tài xế này?")) return;

                    // SỬA: Dùng đường dẫn tương đối (bỏ localhost:8080)
                    fetch("/admin/drivers/delete", { 
                        method: "DELETE",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({
                            driverId: driverId
                        })
                    })
                    .then(response => {
                        if (!response.ok) throw new Error('Delete failed');
                        return response.json(); // Backend cần trả về true/false
                    })
                    .then(data => {
                        if (data === true) {
                            alert("Xóa thành công!");
                            btn.closest("tr").remove(); // Hoặc btn.closest("li") tùy giao diện
                        } else {
                            alert("Xóa thất bại! Có thể tài xế đang có chuyến đi.");
                        }
                    })
                    .catch(error => {
                        console.error(error);
                        alert("Lỗi hệ thống: " + error);
                    });
                });
            });
        }

        // 2. Chuyển hướng xem chi tiết
        function getDetailsController() {
            const detailsBtns = document.querySelectorAll(".update-btn");
            detailsBtns.forEach(btn => {
                btn.addEventListener("click", function () {
                    window.location.href = "/admin/drivers/details/" + btn.dataset.id;
                });
            });
        }

        // 3. Xử lý Tìm kiếm (Search)
        function searchController() {
            const searchBox = document.querySelector(".search-box");
            const searchContainer = document.getElementById("searchCollapsed");
            let timeout;

            if (!searchBox) return; // Kiểm tra null

            searchBox.addEventListener("input", function () {
                clearTimeout(timeout);
                
                // Debounce 500ms để tránh gọi API liên tục
                timeout = setTimeout(() => {
                    const searchTerm = searchBox.value.trim();

                    // Ẩn hiện container kết quả
                    if (searchTerm !== "") {
                        searchContainer.classList.remove("hidden");
                    } else {
                        searchContainer.classList.add("hidden");
                        searchContainer.innerHTML = ""; // Xóa kết quả khi ô tìm kiếm rỗng
                        return;
                    }

                    // SỬA: Dùng đường dẫn tương đối
                    fetch("/admin/drivers/search", { 
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({ searchTerm: searchTerm })
                    })
                    .then(response => response.json())
                    .then(data => {
                        // SỬA LỖI QUAN TRỌNG: Phải xóa kết quả cũ trước khi in kết quả mới
                        searchContainer.innerHTML = ""; 

                        if (data.listDriver && data.listDriver.length > 0) {
                            data.listDriver.forEach(driver => {
                                // Render kết quả tìm kiếm (Thêm link bấm vào để xem chi tiết luôn cho tiện)
                                searchContainer.innerHTML += `
                                    <div class="user-card p-2 border-b hover:bg-gray-100 cursor-pointer" onclick="window.location.href='/admin/drivers/details/${driver.driverId}'">
                                        <div class="font-bold text-green-600">${driver.name}</div>
                                        <div class="text-sm text-gray-500">${driver.phone}</div>
                                        <div class="text-xs text-gray-400">GPLX: ${driver.licenseNumber}</div>
                                    </div>
                                `;
                            });
                        } else {
                            searchContainer.innerHTML = '<div class="p-2 text-gray-500">Không tìm thấy tài xế nào</div>';
                        }
                    })
                    .catch(error => {
                        console.error("Search error:", error);
                    });
                }, 500); // Giảm xuống 500ms cho mượt
            });
            
            // Click ra ngoài thì ẩn bảng tìm kiếm
            document.addEventListener("click", function(event) {
                if (!searchBox.contains(event.target) && !searchContainer.contains(event.target)) {
                    searchContainer.classList.add("hidden");
                }
            });
        }

        // 4. Modal Thêm mới
        function modalController() {
            const addUserOpenBtn = document.querySelector(".add-user-open"); // Nút mở modal
            const addUserCloseBtn = document.querySelector(".add-user-close"); // Nút đóng modal
            const modalContainer = document.querySelector(".modal-container");
            const modal = modalContainer ? modalContainer.querySelector(".modal") : null;
            const addUserForm = document.querySelector("#add-user-form"); // Form thêm driver
            const formSubmitBtn = modal ? modal.querySelector(".add-user-submit") : null;
            
            if (!addUserOpenBtn || !modalContainer) return;

            let isMouseHoveringForm = false;

            // Xử lý đóng/mở
            addUserOpenBtn.addEventListener("click", () => {
                modalContainer.classList.remove("hidden");
                // Animation trượt lên (nếu có class tailwind)
                setTimeout(() => modal.classList.remove("-bottom-full"), 10);
            });

            const closeModal = () => {
                modal.classList.add("-bottom-full");
                setTimeout(() => modalContainer.classList.add("hidden"), 300);
                // Reset form khi đóng
                if(addUserForm) addUserForm.reset(); 
            };

            if(addUserCloseBtn) addUserCloseBtn.addEventListener("click", closeModal);

            // Đóng khi click ra ngoài vùng modal
            modalContainer.addEventListener("click", function () {
                if (!isMouseHoveringForm) closeModal();
            });

            // Kiểm tra chuột có đang ở trong form không
            if (modal) {
                modal.addEventListener("mouseenter", () => isMouseHoveringForm = true);
                modal.addEventListener("mouseleave", () => isMouseHoveringForm = false);
            }

            // Submit form
            if (formSubmitBtn && addUserForm) {
                formSubmitBtn.addEventListener("click", function (e) {
                    e.preventDefault(); // Ngăn submit mặc định để kiểm tra validate nếu cần
                    
                    // Có thể thêm Validate dữ liệu ở đây trước khi submit
                    // if(validateDriverForm()) { ... }

                    addUserForm.submit(); 
                    closeModal();
                });
            }
        }
    });
})();