(function () {
    document.addEventListener("DOMContentLoaded", function () {
        deleteController();
        getDetailsController();
        // searchController();

        function deleteController() {
            // Chọn các nút có class .delete-btn
            const deleteBtns = document.querySelectorAll(".delete-btn");
            
            deleteBtns.forEach(btn => {
                btn.addEventListener("click", function (e) {
                    e.preventDefault(); // Ngăn hành động mặc định
                    const routeId = btn.getAttribute("data-id"); // Lấy ID an toàn hơn
                    const message = `Bạn có chắc muốn xóa tuyến đường #${routeId} không?`;
                    
                    if (confirm(message)) {
                        // SỬA: Gọi đúng API /api/routes/delete
                        fetch("/api/routes/delete", {
                            method: "DELETE",
                            headers: {
                                "Content-Type": "application/json"
                            },
                            body: JSON.stringify({
                                routeId: parseInt(routeId) // Đảm bảo ID là số
                            })
                        })
                        .then(response => {
                            if (!response.ok) {
                                throw new Error('Network response was not ok');
                            }
                            return response.json(); // API trả về boolean
                        })
                        .then(data => {
                            if (data === true) {
                                alert("Xóa thành công!");
                                window.location.reload(); // Tải lại trang
                            } else {
                                alert("Xóa thất bại. Có thể tuyến đường đang được sử dụng.");
                            }
                        })
                        .catch(error => {
                            console.error("Lỗi:", error);
                            alert("Có lỗi xảy ra khi xóa: " + error.message);
                        });
                    }
                });
            });
        }

        function getDetailsController() {
            // Phần này thực ra HTML thẻ <a> đã xử lý rồi, nhưng giữ lại cho search results
            const detailsBtn = document.querySelectorAll(".update-btn");
            detailsBtn.forEach(btn => {
                btn.addEventListener("click", function () {
                    // SỬA: Dẫn về /admin/routes/{id}
                    window.location.href = "/admin/routes/" + btn.dataset.id;
                });
            });
        }

        function searchController() {
            const searchBox = document.querySelector(".search-box"); // Lưu ý: HTML search input cần có class này nếu dùng querySelector
            // Hoặc sửa dòng trên thành: const searchBox = document.querySelector("input[name='keyword']");
            const searchContainer = document.getElementById("search-result-collapse");
            
            // ... (Phần logic search giữ nguyên, chỉ sửa link click) ...
            // Trong đoạn render HTML của search result:
            // onclick="window.location.href='/admin/routes/${route.routeId}'" 
        }
    });

    // Hàm global để update status (được gọi từ onchange trong HTML)
    window.updateRouteStatus = function(selectElement) {
        const routeId = selectElement.getAttribute('data-routeid');
        const newStatus = selectElement.value;

        fetch(`/api/routes/${routeId}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ status: newStatus })
        })
        .then(response => {
            if (response.ok) {
                // Đổi màu text dựa trên status mới
                if(newStatus === 'ACTIVE') {
                    selectElement.classList.remove('text-red-500');
                    selectElement.classList.add('text-emerald-600');
                } else {
                    selectElement.classList.remove('text-emerald-600');
                    selectElement.classList.add('text-red-500');
                }
                // alert('Cập nhật trạng thái thành công'); // Có thể bỏ alert cho mượt
            } else {
                alert('Cập nhật trạng thái thất bại');
                // Reset lại giá trị cũ nếu muốn
            }
        })
        .catch(error => console.error('Error:', error));
    };
})();