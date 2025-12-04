(function () {
    document.addEventListener("DOMContentLoaded", () => {
        const deleteButtons = document.querySelectorAll(".delete-btn");

        deleteButtons.forEach((btn) => {
            btn.addEventListener("click", () => {
                const ticketId = btn.getAttribute("data-id");
                if (!ticketId) return;

                const confirmed = confirm(`Bạn có chắc muốn xóa vé #${ticketId} không?`);
                if (!confirmed) return;

                fetch("/api/tickets/delete", {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({ id: parseInt(ticketId) })
                })
                    .then((response) => response.json())
                    .then((result) => {
                        if (result === true) {
                            alert("Đã xóa vé thành công");
                            window.location.reload();
                        } else {
                            alert("Xóa vé thất bại. Vui lòng thử lại.");
                        }
                    })
                    .catch((error) => {
                        console.error("Lỗi khi xóa vé:", error);
                        alert("Có lỗi xảy ra khi xóa vé.");
                    });
            });
        });
    });
})();