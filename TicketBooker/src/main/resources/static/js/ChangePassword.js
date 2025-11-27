document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector('form');
    const oldPasswordInput = document.querySelector('input[name="oldPassword"]');
    const newPasswordInput = document.querySelector('input[name="newPassword"]');
    const confirmPasswordInput = document.querySelector('input[name="confirmPassword"]');
    // Tìm nút submit để khóa lại khi đang loading
    const submitButton = form.querySelector('button[type="submit"]'); 
    
    const statusMessage = document.createElement('p'); 
    form.appendChild(statusMessage);

    form.addEventListener('submit', async function (event) {
        event.preventDefault(); 
        resetStatusMessage();

        if (!validateForm()) {
            return;
        }

        // 1. Hiệu ứng Loading: Khóa nút & đổi text
        const originalBtnText = submitButton.innerText;
        submitButton.disabled = true;
        submitButton.innerText = "Đang xử lý...";

        const formData = new URLSearchParams();
        formData.append('oldPassword', oldPasswordInput.value);
        formData.append('newPassword', newPasswordInput.value);

        try {
            const response = await fetch('/profile/change-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData.toString()
            });

            // 2. Xử lý kết quả trả về
            const responseText = await response.text(); // Backend trả về text thông báo

            if (response.ok) {
                displaySuccess(responseText || "Đổi mật khẩu thành công!");
                form.reset();
            } else {
                // Hiển thị lỗi từ Backend (ví dụ: "Mật khẩu cũ không đúng")
                displayError(responseText || "Đã xảy ra lỗi.");
            }
        } catch (error) {
            displayError("Lỗi kết nối đến máy chủ.");
        } finally {
            // 3. Mở khóa nút dù thành công hay thất bại
            submitButton.disabled = false;
            submitButton.innerText = originalBtnText;
        }
    });

    // --- CÁC HÀM VALIDATE GIỮ NGUYÊN ---
    function validateForm() {
        resetStatusMessage();

        if (!oldPasswordInput.value.trim()) {
            displayError("Vui lòng nhập mật khẩu cũ.");
            return false;
        }

        if (!validatePasswordStrength(newPasswordInput.value)) {
            displayError("Mật khẩu mới phải có ít nhất 8 ký tự, bao gồm chữ hoa, thường, số và ký tự đặc biệt.");
            return false;
        }

        if (newPasswordInput.value !== confirmPasswordInput.value) {
            displayError("Mật khẩu xác nhận không khớp.");
            return false;
        }
        return true;
    }

    function validatePasswordStrength(password) {
        // Regex này khá chặt chẽ, đảm bảo bảo mật cao
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
        return passwordRegex.test(password);
    }

    function displayError(message) {
        statusMessage.textContent = message;
        statusMessage.className = 'text-danger small mt-2'; // Dùng class Bootstrap (text-danger) hoặc Tailwind (text-red-500) tùy project
    }

    function displaySuccess(message) {
        statusMessage.textContent = message;
        statusMessage.className = 'text-success small mt-2'; // Dùng class Bootstrap (text-success)
    }

    function resetStatusMessage() {
        statusMessage.textContent = '';
        statusMessage.className = '';
    }
});