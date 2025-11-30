document.addEventListener("DOMContentLoaded", function () {
    console.log(">>> User Manager JS Connected & Ready!");

    // --- 1. XỬ LÝ SỰ KIỆN CLICK TOÀN CỤC (Event Delegation) ---
    // Cách này giúp nút bấm VẪN HOẠT ĐỘNG kể cả khi nó được sinh ra sau khi tìm kiếm (Ajax)
    document.body.addEventListener('click', function (event) {
        
        /// A. XỬ LÝ NÚT THÊM (Mở Modal)
if (event.target.closest('.add-user-open')) {
    const modal = document.querySelector(".modal-container");
    const modalContent = document.querySelector(".modal");
    if (modal) {
        modal.classList.remove("hidden");
        modal.style.display = 'flex';
        if (modalContent) {
            modalContent.classList.remove("hidden", "opacity-0", "scale-95", "-bottom-full");
            modalContent.classList.add("opacity-100", "scale-100");
        }
    }
}

// B. XỬ LÝ ĐÓNG MODAL (Nút X hoặc click ra ngoài)
if (event.target.closest('.add-user-close') || event.target.classList.contains('modal-container')) {
    const modal = document.querySelector(".modal-container");
    const modalContent = document.querySelector(".modal");
    if (modal) {
        if (modalContent) {
            modalContent.classList.add("opacity-0", "scale-95", "-bottom-full");
            modalContent.classList.remove("opacity-100", "scale-100");
        }
        setTimeout(() => {
            modal.classList.add("hidden");
            modal.style.display = 'none';
        }, 300); // cùng duration-300 để animation mượt
    }
}


        // C. XỬ LÝ NÚT LƯU (Submit Form Thêm)
        if (event.target.closest('.add-user-submit')) {
            const form = document.getElementById("add-user-form");
            if (form) form.submit();
        }

        // D. XỬ LÝ NÚT XÓA (Delete User)
        const deleteBtn = event.target.closest('.delete-btn'); // Tìm nút xóa gần nhất
        if (deleteBtn) {
            const userId = deleteBtn.getAttribute("data-id"); // Lấy ID từ data-id
            
            if (!userId) {
                console.error("Lỗi: Không tìm thấy ID user để xóa");
                return;
            }

            Swal.fire({
                title: 'Xác nhận xóa?',
                text: `Bạn có chắc muốn xóa User ID #${userId}?`,
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#3085d6',
                confirmButtonText: 'Xóa ngay',
                cancelButtonText: 'Hủy'
            }).then((result) => {
                if (result.isConfirmed) {
                    // GỌI API DELETE
                    fetch("/api/users/delete", {
                        method: "DELETE",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ userId: userId }) 
                    })
                    .then(response => {
                        if (response.ok) {
                            Swal.fire('Thành công', 'Đã xóa người dùng.', 'success');
                            deleteBtn.closest("tr").remove(); // Xóa dòng khỏi bảng ngay lập tức
                        } else {
                            Swal.fire('Lỗi', 'Xóa thất bại.', 'error');
                        }
                    })
                    .catch(error => {
                        console.error(error);
                        Swal.fire('Lỗi', 'Không kết nối được server.', 'error');
                    });
                }
            });
        }
    });

    // --- 2. LOGIC TÌM KIẾM (SEARCH) ---
    const searchBox = document.querySelector(".search-box");
    const resultContainer = document.getElementById("search-result-collapse");
    let timeout = null;

    if (searchBox && resultContainer) {
        searchBox.addEventListener("input", function () {
            clearTimeout(timeout);
            const keyword = this.value.trim();

            if (!keyword) {
                resultContainer.classList.add("hidden");
                return;
            }

            timeout = setTimeout(() => {
                fetch(`/api/users/search?name=${encodeURIComponent(keyword)}`)
                    .then(res => res.json())
                    .then(data => {
                        resultContainer.innerHTML = "";
                        resultContainer.classList.remove("hidden");
                        
                        if (data.listUsers && data.listUsers.length > 0) {
                            data.listUsers.forEach(user => {
                                // Logic hiển thị ảnh/icon
                                let avatarHtml = '<div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center flex-shrink-0"><i class="fas fa-user text-gray-400"></i></div>';
                                if (user.profilePhoto) {
                                    avatarHtml = `<img src="/profile/photo/${user.userId}" class="w-10 h-10 rounded-full object-cover flex-shrink-0">`;
                                }

                                const html = `
                                    <div class="flex items-center p-3 hover:bg-gray-100 cursor-pointer border-b transition"
                                         onclick="window.location.href='/admin/users/details/${user.userId}'">
                                        <div class="mr-3">${avatarHtml}</div>
                                        <div>
                                            <div class="font-bold text-gray-800">${user.fullName}</div>
                                            <div class="text-xs text-gray-500">${user.email}</div>
                                        </div>
                                    </div>`;
                                resultContainer.insertAdjacentHTML("beforeend", html);
                            });
                        } else {
                            resultContainer.innerHTML = '<div class="p-3 text-gray-500 text-center">Không tìm thấy.</div>';
                        }
                    });
            }, 500);
        });

        // Ẩn khi click ra ngoài
        document.addEventListener("click", (e) => {
            if (!searchBox.contains(e.target) && !resultContainer.contains(e.target)) {
                resultContainer.classList.add("hidden");
            }
        });
    }
});

// --- CÁC HÀM ONCHANGE (Cần gán vào window để HTML gọi được) ---
window.updateStatus = function(selectElement) {
    const userId = selectElement.getAttribute("data-userid");
    const newStatus = selectElement.value;
    fetch(`/api/users/${userId}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: newStatus })
    }).then(res => {
        if(res.ok) Swal.fire({ toast: true, position: 'top-end', icon: 'success', title: 'Đã cập nhật', showConfirmButton: false, timer: 1000 });
    });
};

window.updateRole = function(selectElement) {
    const userId = selectElement.getAttribute("data-userid");
    const newRole = selectElement.value;
    fetch(`/api/users/${userId}/role`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ role: newRole })
    }).then(res => {
        if(res.ok) Swal.fire({ toast: true, position: 'top-end', icon: 'success', title: 'Đã cập nhật', showConfirmButton: false, timer: 1000 });
    });
};