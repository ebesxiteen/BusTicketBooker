tailwind.config = {
    theme: {
        extend: {
            colors: {
                // Đổi từ Tím sang Xanh Lá GreenBus
                primary: '#10b981',   /* Màu chính (Xanh ngọc - Emerald 500) */
                secondary: '#047857', /* Màu phụ đậm (Emerald 700) */
                accent: '#d1fae5'     /* Màu nền nhạt (Emerald 100) - Thêm vào để dùng nếu cần */
            }
        }
    }
}

// Giữ nguyên phần khởi tạo hiệu ứng
AOS.init({
    duration: 800,
    easing: 'ease-out-cubic',
    once: true
});