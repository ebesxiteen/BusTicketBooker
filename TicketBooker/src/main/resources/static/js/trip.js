// 1. Khởi tạo & Event Listeners
document.addEventListener("DOMContentLoaded", function () {
    // Khóa chọn ngày quá khứ
    var now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset()); // Fix timezone
    var today = now.toISOString().slice(0, 16);
    
    const departureTimeInput = document.getElementById("departureTime");
    if(departureTimeInput) departureTimeInput.setAttribute("min", today);

    // Khởi tạo các controller
    editTripController();
    initModalEvents();
});

// 2. Xử lý Xóa Chuyến Xe
function confirmDeleteTrip(button) {
    const tripContainer = button.closest('.trip-container');
    const tripId = tripContainer.querySelector('.trip-id').textContent.trim();

    Swal.fire({
        title: 'Xác nhận',
        text: `Bạn có chắc muốn xóa chuyến xe ID ${tripId}?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Có, xóa!',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            deleteTrip(tripId, tripContainer);
        }
    });
}

function deleteTrip(tripId, tripContainer) {
    // SỬA: Dùng đường dẫn tương đối chuẩn
    fetch('/admin/trips/delete', { // Backend cần mapping đúng URL này
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tripId: tripId })
    })
    .then(response => {
        if (response.ok) {
            Swal.fire('Đã xóa!', `Chuyến xe ${tripId} đã bị xóa.`, 'success');
            tripContainer.remove();
        } else {
            Swal.fire('Lỗi', 'Xóa thất bại.', 'error');
        }
    })
    .catch(error => {
        console.error(error);
        Swal.fire('Lỗi', 'Lỗi kết nối server.', 'error');
    });
}

// 3. Xử lý Modal Thêm mới & Load dữ liệu động
function initModalEvents() {
    // Sự kiện thay đổi Điểm đi -> Load Điểm đến tương ứng
    const departureSelect = document.querySelector("#departureLocation");
    if(departureSelect) {
        departureSelect.addEventListener('change', function () {
            const selectedDeparture = this.value;
            if(!selectedDeparture) return;

            // Load Arrival Locations
            fetch('/admin/routes/getArrivalLocation?departureLocation=' + encodeURIComponent(selectedDeparture))
                .then(response => response.json())
                .then(data => {
                    let options = '<option value="">Chọn điểm đến</option>';
                    data.forEach(route => {
                        // Value của option là routeId để gán vào form
                        options += `<option value="${route.routeId}">${route.arrivalLocation}</option>`;
                    });
                    document.querySelector('#arrivalLocation').innerHTML = options;
                })
                .catch(err => console.error("Lỗi load điểm đến:", err));
        });
    }

    // Sự kiện khi chọn Điểm đến -> Gán RouteId vào input ẩn
    const arrivalSelect = document.getElementById("arrivalLocation");
    if(arrivalSelect) {
        arrivalSelect.addEventListener("change", function() {
            // Value của option chính là routeId
            document.getElementById("routeId").value = this.value;
        });
    }
}

// Hàm mở Modal Thêm mới
window.openAddTripModal = function () {
    const modal = document.querySelector('#addTripModal');
    modal.classList.remove('hidden');

    // Load danh sách Điểm khởi hành (Departure)
    fetch('/admin/routes/getDepartureLocation')
        .then(response => response.json())
        .then(data => {
            let options = '<option value="">Chọn điểm khởi hành</option>';
            data.forEach(loc => {
                options += `<option value="${loc}">${loc}</option>`;
            });
            document.querySelector('#departureLocation').innerHTML = options;
        })
        .catch(err => console.error("Lỗi load điểm đi:", err));

    // Load danh sách Tài xế
    fetch('/api/drivers/getAll') // API này cần trả về danh sách Driver
        .then(response => response.json())
        .then(data => {
            let options = '<option value="">Chọn tài xế</option>';
            // Kiểm tra cấu trúc trả về (data.listDriver hay data trực tiếp)
            const list = data.listDriver || data; 
            list.forEach(driver => {
                options += `<option value="${driver.driverId}">${driver.name}</option>`;
            });
            document.querySelector('#driverName').innerHTML = options;
        })
        .catch(err => console.error("Lỗi load tài xế:", err));
    
    // Load danh sách Xe (Bus) - Bổ sung thêm nếu cần
    fetch('/api/buses/getAll') 
        .then(response => response.json())
        .then(data => {
            let options = '<option value="">Chọn xe</option>';
            const list = data.listBus || data;
            list.forEach(bus => {
                options += `<option value="${bus.busId}">${bus.licensePlate} (${bus.busType})</option>`;
            });
            document.querySelector('#license').innerHTML = options;
        })
        .catch(err => console.error("Lỗi load xe:", err));
}

// 4. Submit Form Thêm mới
function submitAddTripForm() {
    const routeId = document.getElementById('routeId').value;
    const busId = document.getElementById('busId').value;
    const driverId = document.getElementById('driverId').value;
    const departureTime = document.getElementById('departureTime').value;
    const price = document.getElementById('price').value;

    if (!routeId || !busId || !driverId || !departureTime || !price) {
        Swal.fire({
            icon: 'warning',
            title: 'Thiếu thông tin!',
            text: 'Vui lòng chọn đầy đủ Lộ trình, Xe, Tài xế và Thời gian.',
            confirmButtonText: 'OK'
        });
        return;
    }

    Swal.fire({
        icon: 'success',
        title: 'Thành công',
        text: 'Đang tạo chuyến xe...',
        timer: 1500,
        showConfirmButton: false
    }).then(() => {
        document.getElementById('addTripForm').submit();
    });
}

// 5. Xử lý Sửa Chuyến Xe
function editTripController() {
    window.editTrip = function(button) {
        const tripContainer = button.closest('.trip-container');

        // Lấy dữ liệu từ giao diện (Hidden fields hoặc Text)
        const getVal = (selector) => tripContainer.querySelector(selector)?.textContent.trim() || "";

        const data = {
            id: getVal('.trip-id'),
            routeId: getVal('.route-id'),
            busId: getVal('.bus-id'),
            driverId: getVal('.driver-id'),
            status: getVal('.status'),
            depStation: getVal('.departure-station'),
            arrStation: getVal('.arrival-station'),
            depTime: getVal('.departureTime'),
            arrTime: getVal('.arrivalTime'),
            price: getVal('.price span'),
            seats: getVal('.availableSeats span')
        };

        // Đổ dữ liệu vào Modal Sửa
        const modal = document.getElementById('updateTripModal');
        modal.classList.remove('hidden');

        document.getElementById('edittripId').value = data.id;
        document.getElementById('editrouteId').value = data.routeId;
        document.getElementById('editbusId').value = data.busId;
        document.getElementById('editdriverId').value = data.driverId;
        
        // ... Gán các trường còn lại tương tự ...
        // Lưu ý: Cần đảm bảo ID của input trong modal sửa khớp với code này
    };
}

// Xử lý đóng modal khi click ra ngoài hoặc nút hủy (Chung cho cả 2 modal)
window.closeModal = function(modalId) {
    document.getElementById(modalId).classList.add('hidden');
}