tailwind.config = {
    theme: {
        extend: {
            colors: {
                // Đổi tên từ 'purple' sang 'primary' để dễ tái sử dụng
                // Hoặc bạn có thể đặt tên là 'greenbus'
                primary: {
                    light: '#6ee7b7',    /* Emerald 300 - Dùng cho hover nhẹ, viền */
                    DEFAULT: '#10b981',  /* Emerald 500 - MÀU CHÍNH (Logo, Button) */
                    dark: '#047857',     /* Emerald 700 - Dùng cho hover đậm, text đậm */
                },
                // Thêm màu phụ nếu cần
                secondary: '#d1fae5',    /* Emerald 100 - Nền rất nhạt */
            }
        }
    }
}