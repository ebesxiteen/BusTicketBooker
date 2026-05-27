(function () {
    document.addEventListener("DOMContentLoaded", function () {
        deleteController();
    });

    // 1. Controller Xóa Xe
    function deleteController() {
        const deleteBtns = document.querySelectorAll(".delete-btn");
        
        deleteBtns.forEach(btn => {
            btn.addEventListener("click", function (e) {
                e.preventDefault();
                const busId = btn.getAttribute("data-id");
                
                // Dùng confirm mặc định hoặc SweetAlert nếu có
                if (confirm(`Bạn có chắc muốn xóa xe #${busId} không?`)) {
                    
                    // Gọi API Xóa (Đường dẫn /api/buses/{id})
                    fetch(`/api/buses/${busId}`, {
                        method: "DELETE"
                    })
                    .then(response => {
                        if (response.ok) {
                            alert("Xóa xe thành công!");
                            window.location.reload();
                        } else {
                            alert("Xóa thất bại! Có thể xe đang được sử dụng trong chuyến đi.");
                        }
                    })
                    .catch(error => {
                        console.error("Error:", error);
                        alert("Lỗi hệ thống khi xóa xe.");
                    });
                }
            });
        });
    }

    // 2. Hàm global cập nhật trạng thái (gọi từ onchange trong HTML)
    window.updateBusStatus = function(selectElement) {
        const busId = selectElement.getAttribute('data-busid');
        const newStatus = selectElement.value;

        fetch(`/api/buses/${busId}/status`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ busStatus: newStatus })
        })
        .then(response => {
            if (response.ok) {
                // Đổi màu text
                if(newStatus === 'ACTIVE') {
                    selectElement.classList.remove('text-gray-400');
                    selectElement.classList.add('text-emerald-600');
                } else {
                    selectElement.classList.remove('text-emerald-600');
                    selectElement.classList.add('text-gray-400');
                }
                // alert('Cập nhật trạng thái thành công!');
            } else {
                alert('Cập nhật trạng thái thất bại!');
                // Reset lại giá trị cũ nếu cần
                // selectElement.value = oldStatus;
            }
        })
        .catch(error => console.error('Error:', error));
    };

})();