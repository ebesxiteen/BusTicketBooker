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
        method: "DELETE", // Đã thống nhất dùng DELETE
        headers: { 
            "Content-Type": "application/json" 
            // Thêm CSRF token nếu cần
        },
        body: JSON.stringify({ tripId: tripId })
    })
    .then((response) => {
        // KỊCH BẢN THÀNH CÔNG (HTTP 200)
        if (response.ok) {
            Swal.fire("Đã xóa!", "Chuyến xe đã được xóa.", "success")
            .then(() => {
                if (row) row.remove(); // Xóa dòng khỏi bảng
                // window.location.reload(); 
            });
        } 
        // KỊCH BẢN CÓ LỖI (HTTP 400, 500...)
        else {
            // response.text() sẽ lấy dòng chữ: "Không thể xóa! Chuyến xe đang có..."
            return response.text().then(errorMessage => {
                Swal.fire({
                    icon: 'error',
                    title: 'Không thể xóa!',
                    text: errorMessage, // <--- Hiển thị lỗi từ Java tại đây
                    confirmButtonColor: '#d33'
                });
            });
        }
    })
    .catch((err) => {
        // Lỗi mạng hoặc server sập
        console.error(err);
        Swal.fire("Lỗi kết nối!", "Không thể gọi đến server.", "error");
    });
}
            });
        });
    });
});