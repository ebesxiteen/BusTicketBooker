document.addEventListener('DOMContentLoaded', function () {
    // Lấy root của trang lịch sử vé
    const root = document.getElementById('ticket-history-root');
    if (!root) {
        console.log('[TicketHistory] Không tìm thấy #ticket-history-root');
        return;
    }

    // Lấy URL API từ data attribute Thymeleaf gắn
    const cancelApi = root.dataset.cancelApi;
    console.log('[TicketHistory] CANCEL API =', cancelApi);

    // Tìm tất cả nút hủy vé
    const buttons = root.querySelectorAll('.btn-cancel-ticket');
    console.log('[TicketHistory] Số nút Hủy vé tìm được =', buttons.length);

    if (!buttons.length) {
        return;
    }

    buttons.forEach(function (btn) {
        btn.addEventListener('click', async function () {
            const ticketId = btn.getAttribute('data-ticket-id');
            console.log('[TicketHistory] Click Hủy vé', ticketId);

            if (!ticketId) {
                alert('Không xác định được mã vé.');
                return;
            }

            const ok = confirm('Bà có chắc muốn hủy vé #' + ticketId + ' không?');
            if (!ok) return;

            try {
                const response = await fetch(cancelApi, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    // ⚠ Nếu TicketIdRequest có field "ticketId":
                    body: JSON.stringify({ id: ticketId })
                    // Nếu class TicketIdRequest là { Integer id; } thì đổi thành:
                    // body: JSON.stringify({ id: ticketId })
                });

                if (!response.ok) {
                    alert('Hủy vé thất bại (HTTP ' + response.status + ').');
                    return;
                }

                const result = await response.json(); // TicketApi.cancelTicket trả boolean
                if (result === true) {
                    alert('Đã hủy vé #' + ticketId + ' thành công.');
                    window.location.reload();
                } else {
                    alert('Không thể hủy vé. Có thể vé đã được sử dụng hoặc xảy ra lỗi.');
                }
            } catch (err) {
                console.error(err);
                alert('Có lỗi xảy ra khi gọi API hủy vé.');
            }
        });
    });
});
