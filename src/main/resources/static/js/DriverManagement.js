(function () {
    document.addEventListener("DOMContentLoaded", function () {
        // Khởi tạo các bộ điều khiển sự kiện khi trang tải xong
        deleteController();
        
        // Nếu bạn muốn giữ lại chức năng click vào dòng để xem chi tiết (tùy chọn)
        rowClickController();
    });

    // 1. XỬ LÝ SỰ KIỆN XÓA TÀI XẾ
    function deleteController() {
        // Tìm tất cả các nút có class '.delete-btn' (đã thêm trong HTML mới)
        const deleteBtns = document.querySelectorAll(".delete-btn");
        
        deleteBtns.forEach(btn => {
            btn.addEventListener("click", function (e) {
                e.preventDefault(); // Ngăn chặn hành động mặc định của nút/thẻ a
                e.stopPropagation(); // Ngăn sự kiện click lan ra dòng (nếu có rowClickController)
                
                const driverId = btn.getAttribute("data-id");
                
                // Hiển thị hộp thoại xác nhận
                if (confirm(`Bạn có chắc chắn muốn xóa tài xế #${driverId}? Hành động này không thể hoàn tác.`)) {
                    
                    // Gọi API DELETE theo chuẩn RESTful: /api/drivers/{id}
                    fetch(`/api/drivers/${driverId}`, {
                        method: "DELETE"
                    })
                    .then(response => {
                        if (response.ok) {
                            alert("Xóa tài xế thành công!");
                            // Reload lại trang để cập nhật danh sách mới nhất
                            window.location.reload(); 
                        } else {
                            // Nếu server trả về lỗi (VD: do ràng buộc khóa ngoại)
                            return response.text().then(errorMessage => {
                                alert("Xóa thất bại: " + errorMessage);
                            });
                        }
                    })
                    .catch(error => {
                        console.error("Lỗi hệ thống:", error);
                        alert("Đã xảy ra lỗi khi kết nối đến server.");
                    });
                }
            });
        });
    }

    // 2. (Tùy chọn) CLICK VÀO DÒNG ĐỂ XEM CHI TIẾT
    // Hàm này giúp người dùng bấm vào bất kỳ đâu trên dòng cũng xem được, không cần bấm chính xác nút Sửa
    function rowClickController() {
        const rows = document.querySelectorAll("tbody tr");
        rows.forEach(row => {
            row.addEventListener("click", function(e) {
                // Nếu click vào nút Xóa hoặc Select box thì không chuyển trang
                if (e.target.closest('.delete-btn') || e.target.closest('select')) return;

                // Tìm nút sửa trong dòng đó để lấy ID (hoặc lấy từ data-id của nút xóa)
                const editBtn = row.querySelector("a[href*='/admin/drivers/']");
                if (editBtn) {
                    window.location.href = editBtn.getAttribute("href");
                }
            });
            // Thêm style con trỏ chuột để user biết là bấm được
            row.style.cursor = "pointer";
        });
    }

    // 3. XỬ LÝ CẬP NHẬT TRẠNG THÁI (Global Function)
    // Hàm này phải khai báo dạng window.func để HTML có thể gọi trực tiếp qua onchange="..."
    window.updateDriverStatus = function(selectElement) {
        const driverId = selectElement.getAttribute('data-id');
        const newStatus = selectElement.value;

        // Gọi API PATCH để cập nhật trạng thái
        fetch(`/api/drivers/${driverId}/status`, {
            method: 'PATCH',
            headers: { 
                'Content-Type': 'application/json' 
            },
            body: JSON.stringify({ driverStatus: newStatus })
        })
        .then(response => {
            if (response.ok) {
                // Cập nhật màu sắc hiển thị ngay lập tức để tăng trải nghiệm người dùng
                if (newStatus === 'ACTIVE') {
                    selectElement.classList.remove('text-gray-400');
                    selectElement.classList.add('text-emerald-600');
                } else {
                    selectElement.classList.remove('text-emerald-600');
                    selectElement.classList.add('text-gray-400');
                }
                // Có thể hiện thông báo nhỏ hoặc bỏ qua cho mượt
                // console.log("Cập nhật trạng thái thành công");
            } else {
                alert('Cập nhật trạng thái thất bại!');
                // Reset lại giá trị cũ nếu muốn (cần lưu giá trị cũ trước khi đổi)
            }
        })
        .catch(error => {
            console.error("Lỗi cập nhật trạng thái:", error);
            alert("Lỗi kết nối server.");
        });
    };

})();