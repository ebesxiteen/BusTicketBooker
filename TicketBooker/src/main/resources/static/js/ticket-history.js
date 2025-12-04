document.addEventListener('DOMContentLoaded', function () {
    // Lấy root của trang lịch sử vé
    const root = document.getElementById('ticket-history-root');
    if (!root) {
        console.log('[TicketHistory] Không tìm thấy #ticket-history-root');
        return;
    }

    console.log('[TicketHistory] Không còn hành động hủy vé trong lịch sử vé.');
});
