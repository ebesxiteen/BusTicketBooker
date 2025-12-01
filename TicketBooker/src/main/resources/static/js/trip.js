document.addEventListener("DOMContentLoaded", function () {
    console.log(">>> [DEBUG] trip.js ĐÃ ĐƯỢC LOAD!"); // 1. Kiểm tra file có load không

    // Tìm tất cả các nút có class .delete-trip-btn
    const deleteButtons = document.querySelectorAll('.delete-trip-btn');
    
    console.log(">>> [DEBUG] Tìm thấy " + deleteButtons.length + " nút xóa."); // 2. Kiểm tra có tìm thấy nút không

    if (deleteButtons.length === 0) {
        console.warn(">>> [CẢNH BÁO] Không tìm thấy nút xóa nào. Kiểm tra lại class HTML!");
    }

    // Gắn sự kiện Click cho từng nút
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function (event) {
            event.preventDefault(); // Chặn hành động mặc định (nếu có)
            
            const tripId = this.getAttribute("data-id");
            const row = this.closest("tr");

            console.log(">>> [DEBUG] Đang click nút xóa ID: " + tripId);

            // SweetAlert Confirm
            Swal.fire({
                title: "Xác nhận xóa?",
                text: `Bạn có chắc muốn xóa chuyến xe ID: ${tripId}?`,
                icon: "warning",
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#3085d6',
                confirmButtonText: "Xóa ngay",
                cancelButtonText: "Hủy"
            }).then((result) => {
                if (result.isConfirmed) {
                    fetch("/api/trips/delete", {
                        method: "DELETE",
                        headers: { 
                            "Content-Type": "application/json" 
                            // Nếu có lỗi 403 Forbidden thì cần thêm header CSRF ở đây
                        },
                        body: JSON.stringify({ tripId: tripId })
                    })
                    .then((response) => {
                        if (response.ok) {
                            Swal.fire("Đã xóa!", "Chuyến xe đã được xóa.", "success")
                            .then(() => {
                                // Xóa dòng khỏi bảng ngay lập tức (hiệu ứng mượt hơn reload)
                                if (row) row.remove();
                                // window.location.reload(); // Hoặc reload nếu muốn chắc chắn
                            });
                        } else {
                            return response.text().then(text => {
                                Swal.fire("Lỗi!", text, "error");
                            });
                        }
                    })
                    .catch((err) => {
                        Swal.fire("Lỗi server!", "Không kết nối được API.", "error");
                        console.error(err);
                    });
                }
            });
        });
    });
});