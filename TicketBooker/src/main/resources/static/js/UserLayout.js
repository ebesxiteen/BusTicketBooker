document.addEventListener("DOMContentLoaded", function () {
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const closeMobileMenuBtn = document.getElementById('closeMobileMenuBtn');
    const mobileMenu = document.getElementById('mobileMenu');

    // Kiểm tra xem các phần tử có tồn tại không (để tránh lỗi ở các trang không có menu)
    if (mobileMenuBtn && mobileMenu) {
        
        // 1. Mở Menu
        mobileMenuBtn.addEventListener('click', function (e) {
            e.stopPropagation(); // Ngăn sự kiện click lan ra ngoài
            mobileMenu.classList.add('show'); // Thêm class 'show' để kích hoạt CSS transform
        });

        // 2. Đóng Menu (Nút X)
        if (closeMobileMenuBtn) {
            closeMobileMenuBtn.addEventListener('click', function () {
                mobileMenu.classList.remove('show');
            });
        }

        // 3. Đóng Menu khi click ra ngoài vùng menu
        document.addEventListener('click', function (event) {
            // Nếu click KHÔNG nằm trong menu VÀ menu đang mở
            if (!mobileMenu.contains(event.target) && mobileMenu.classList.contains('show')) {
                mobileMenu.classList.remove('show');
            }
        });
    }
});