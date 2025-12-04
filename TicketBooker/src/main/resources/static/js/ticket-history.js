document.addEventListener('DOMContentLoaded', function () {
    // Lấy root của trang lịch sử vé
    const root = document.getElementById('ticket-history-root');
    if (!root) {
        console.log('[TicketHistory] Không tìm thấy #ticket-history-root');
        return;
    }

    const cancelButtons = root.querySelectorAll('.cancel-ticket-btn');

    if (!cancelButtons.length) {
        console.log('[TicketHistory] Không còn hành động hủy vé trong lịch sử vé.');
        return;
    }

    cancelButtons.forEach((button) => {
        button.addEventListener('click', async () => {
            const ticketId = button.dataset.ticketId;
            const route = button.dataset.route;
            const departure = button.dataset.departure;
            const seat = button.dataset.seat || 'Không xác định';

            const confirmed = window.confirm(
                `Bạn có chắc muốn hủy vé #${ticketId}?\n\n` +
                `Tuyến: ${route}\n` +
                `Giờ đi: ${departure}\n` +
                `Ghế: ${seat}`
            );

            if (!confirmed) {
                return;
            }

            const originalLabel = button.innerHTML;
            button.disabled = true;
            button.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Đang hủy...</span>';

            try {
                const response = await fetch('/api/tickets/cancel-ticket', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ id: Number(ticketId) }),
                });

                if (!response.ok) {
                    throw new Error(`Server trả về lỗi ${response.status}`);
                }

                const result = await response.json();

                if (result === true) {
                    alert('Hủy vé thành công. Trang sẽ được tải lại để cập nhật.');
                    window.location.reload();
                } else {
                    alert('Không thể hủy vé này. Vui lòng thử lại hoặc liên hệ hỗ trợ.');
                }
            } catch (error) {
                console.error('[TicketHistory] Lỗi khi hủy vé:', error);
                alert('Đã xảy ra lỗi khi hủy vé. Vui lòng thử lại sau.');
            } finally {
                button.disabled = false;
                button.innerHTML = originalLabel;
            }
        });
    });
});
