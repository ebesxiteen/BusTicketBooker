(function () {
    document.addEventListener("DOMContentLoaded", function () {
        // Khởi tạo các controller
        modalController();
        deleteController();
        getDetailsController();
        searchController();
        updateController();

        // 1. Xử lý Upload Avatar
        function updateController() {
            const fileInput = document.querySelector(".avatar-input");
            if (fileInput) {
                const avatarShow = fileInput.closest("div").querySelector("img"); // Tìm ảnh gần nhất (sửa lại selector cho chắc chắn)
                
                fileInput.addEventListener('change', function (event) {
                    const file = event.target.files[0];
                    if (file && avatarShow) {
                        const reader = new FileReader();
                        reader.onload = function (e) {
                            avatarShow.src = e.target.result;
                        };
                        reader.readAsDataURL(file);
                    }
                });
            }
        }

        // 2. Xử lý Xóa User
        function deleteController() {
            const deleteBtns = document.querySelectorAll(".delete-btn");
            deleteBtns.forEach(btn => {
                btn.addEventListener("click", function (e) {
                    if(!confirm("Bạn có chắc muốn xóa người dùng này?")) return;

                    const userId = btn.dataset.id;
                    // SỬA URL: Dùng đường dẫn tương đối
                    fetch("/api/users/delete", { 
                        method: "DELETE",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ userId: userId })
                    })
                    .then(response => {
                        if (!response.ok) throw new Error('Delete failed');
                        return response.text(); // Backend trả về String hoặc Boolean
                    })
                    .then(data => {
                        alert("Xóa thành công!");
                        btn.closest("tr").remove(); // Hoặc btn.closest("li")
                    })
                    .catch(error => {
                        console.error(error);
                        alert("Lỗi khi xóa người dùng!");
                    });
                });
            });
        }

        // 3. Chuyển hướng xem chi tiết
        function getDetailsController() {
            const detailsBtns = document.querySelectorAll(".details-btn");
            detailsBtns.forEach(btn => {
                btn.addEventListener("click", function () {
                    window.location.href = "/admin/users/details/" + btn.dataset.id;
                });
            });
        }

        // 4. Tìm kiếm User (QUAN TRỌNG: SỬA HTML RENDER)
        function searchController() {
            const searchBox = document.querySelector(".search-box");
            const searchContainer = document.getElementById("search-result-collapse");
            let timeout;

            if (!searchBox) return;

            searchBox.addEventListener("input", function () {
                clearTimeout(timeout);
                timeout = setTimeout(() => {
                    const keyword = searchBox.value.trim();

                    if (keyword !== "") {
                        searchContainer.classList.remove("h-0", "hidden");
                        searchContainer.classList.add("h-fit");
                    } else {
                        searchContainer.classList.add("h-0", "hidden");
                        searchContainer.classList.remove("h-fit");
                        searchContainer.innerHTML = ""; // Xóa kết quả khi rỗng
                        return;
                    }

                    // SỬA URL: Chuyển sang GET request cho đúng chuẩn tìm kiếm
                    // (Backend UserApi đã đổi thành @GetMapping("/search"))
                    fetch(`/api/users/search?name=${encodeURIComponent(keyword)}`)
                        .then(response => response.json())
                        .then(response => {
                            // XÓA KẾT QUẢ CŨ TRƯỚC KHI THÊM MỚI
                            searchContainer.innerHTML = "";

                            if (response.listUsers && response.listUsers.length > 0) {
                                response.listUsers.forEach(user => {
                                    // SỬA HTML: Lấy user.email trực tiếp (không qua user.account)
                                    // Sửa đường dẫn ảnh no-avatar
                                    let avatarSrc = user.profilePhoto ? user.profilePhoto : '/images/default-avatar.png'; 
                                    
                                    searchContainer.innerHTML += `
                                        <div class="shadow hover:bg-gray-100 flex justify-start w-full items-center p-3 space-x-4 cursor-pointer border-b search-data" data-id="${user.userId}">
                                            <div class="w-10 h-10 rounded-full bg-gray-200 overflow-hidden">
                                                <img src="${avatarSrc}" class="w-full h-full object-cover">
                                            </div>
                                            <div class="flex flex-col items-start justify-center">
                                                <div class="font-bold text-gray-700">${user.fullName}</div>
                                                <div class="text-sm text-gray-500">${user.email || 'No email'}</div>
                                                <div class="text-xs text-gray-400">${user.phone || ''}</div>
                                            </div>
                                        </div>
                                    `;
                                });

                                // Gán sự kiện click cho các item vừa tạo
                                const searchData = searchContainer.querySelectorAll(".search-data");
                                searchData.forEach(data => {
                                    data.addEventListener("click", function () {
                                        window.location.href = "/admin/users/details/" + data.dataset.id;
                                    });
                                });
                            } else {
                                searchContainer.innerHTML = '<div class="p-3 text-gray-500">Không tìm thấy người dùng</div>';
                            }
                        })
                        .catch(error => {
                            console.error("Search error:", error);
                        });
                }, 500); // 500ms delay
            });
            
            // Ẩn khi click ra ngoài
            document.addEventListener('click', function(e) {
                if(!searchBox.contains(e.target) && !searchContainer.contains(e.target)) {
                    searchContainer.classList.add("h-0", "hidden");
                }
            });
        }

        // 5. Modal Thêm User
        function modalController() {
            const addUserOpenBtn = document.querySelector(".add-user-open");
            const addUserCloseBtn = document.querySelector(".add-user-close");
            const modalContainer = document.querySelector(".modal-container");
            const modal = modalContainer ? modalContainer.querySelector(".modal") : null;
            const addUserForm = document.querySelector("#add-user-form");
            const formSubmitBtn = modal ? modal.querySelector(".add-user-submit") : null;
            
            if(!addUserOpenBtn || !modalContainer) return;

            let isMouseHoveringForm = false;

            const openModal = () => {
                modalContainer.classList.remove("hidden");
                setTimeout(() => modal.classList.remove("-bottom-full"), 10);
            };

            const closeModal = () => {
                modal.classList.add("-bottom-full");
                setTimeout(() => modalContainer.classList.add("hidden"), 300);
                if(addUserForm) addUserForm.reset();
            };

            addUserOpenBtn.addEventListener("click", openModal);
            if(addUserCloseBtn) addUserCloseBtn.addEventListener("click", closeModal);

            modalContainer.addEventListener("click", function () {
                if (!isMouseHoveringForm) closeModal();
            });

            if (modal) {
                modal.addEventListener("mouseenter", () => isMouseHoveringForm = true);
                modal.addEventListener("mouseleave", () => isMouseHoveringForm = false);
            }

            if(formSubmitBtn) {
                formSubmitBtn.addEventListener("click", function () {
                    // Validate sơ bộ nếu cần
                    addUserForm.submit();
                    closeModal();
                });
            }
        }
    });
})();