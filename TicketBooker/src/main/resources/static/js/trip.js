document.addEventListener("DOMContentLoaded", function () {
    console.log("Trip JS Loaded!");

    // SweetAlert confirm delete
    document.body.addEventListener("click", function (event) {
        const btn = event.target.closest(".delete-trip-btn");
        if (!btn) return;

        const tripId = btn.getAttribute("data-id");
        const row = btn.closest("tr");

        Swal.fire({
            title: "Xác nhận xóa?",
            text: `Bạn có chắc muốn xóa chuyến xe ID: ${tripId}?`,
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Xóa",
            cancelButtonText: "Hủy"
        }).then((result) => {
            if (result.isConfirmed) {
                fetch("/api/trips/delete", {
                    method: "DELETE",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ tripId: tripId })
                })
                    .then((response) => {
                        if (response.ok) {
                            Swal.fire("Đã xóa!", "Chuyến xe đã được xóa.", "success");
                            row.remove();
                        } else {
                            Swal.fire("Lỗi!", "Không thể xóa chuyến xe.", "error");
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
