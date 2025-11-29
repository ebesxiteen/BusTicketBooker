document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector('#passwordChangeForm');
    const oldPasswordInput = document.querySelector('#oldPassword');
    const newPasswordInput = document.querySelector('#newPassword');
    const confirmPasswordInput = document.querySelector('#confirmPassword');
    const submitButton = form.querySelector('button[type="submit"]');

    // Container để hiển thị card thông báo
    const messageContainer = document.createElement('div');
    form.appendChild(messageContainer);

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        clearMessages();

        if (!validateForm()) {
            return;
        }

        const originalBtnText = submitButton.innerText;
        submitButton.disabled = true;
        submitButton.innerText = "Đang xử lý...";

        const formData = new URLSearchParams();
        formData.append('oldPassword', oldPasswordInput.value);
        formData.append('newPassword', newPasswordInput.value);

        try {
            const response = await fetch('/profile/change-password', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData.toString()
            });

            const responseText = await response.text();

            if (response.ok) {
                showCard(responseText || "Đổi mật khẩu thành công!", true);
                form.reset();
            } else {
                showCard(responseText || "Đã xảy ra lỗi.", false);
            }
        } catch (error) {
            showCard("Lỗi kết nối đến máy chủ.", false);
        } finally {
            submitButton.disabled = false;
            submitButton.innerText = originalBtnText;
        }
    });

    // ==========================
    // VALIDATE
    // ==========================
    function validateForm() {
        if (!oldPasswordInput.value.trim()) {
            showFieldError(oldPasswordInput, "Vui lòng nhập mật khẩu cũ.");
            return false;
        }
        if (!validatePasswordStrength(newPasswordInput.value)) {
            showFieldError(newPasswordInput, "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return false;
        }
        if (newPasswordInput.value === oldPasswordInput.value) {
            showFieldError(newPasswordInput, "Mật khẩu mới không được trùng mật khẩu cũ.");
            return false;
        }
        if (newPasswordInput.value !== confirmPasswordInput.value) {
            showFieldError(confirmPasswordInput, "Mật khẩu xác nhận không khớp.");
            return false;
        }
        return true;
    }

    function validatePasswordStrength(password) {
        // Chỉ cần tối thiểu 6 ký tự
        return password.length >= 6;
    }

    // ==========================
    // HIỂN THỊ CARD THÔNG BÁO
    // ==========================
    function showCard(message, isSuccess) {
        const card = document.createElement('div');
        card.className = `p-4 rounded-lg shadow-md mt-4 ${isSuccess ? 'bg-green-100 border border-green-400 text-green-700' : 'bg-red-100 border border-red-400 text-red-700'}`;
        card.innerHTML = `
            <div class="flex items-center">
                <i class="fas ${isSuccess ? 'fa-check-circle text-green-600' : 'fa-exclamation-circle text-red-600'} mr-2"></i>
                <span class="font-semibold">${message}</span>
            </div>
        `;
        messageContainer.appendChild(card);
    }

    function showFieldError(input, message) {
        const error = document.createElement('div');
        error.className = 'text-red-500 text-sm mt-1';
        error.textContent = message;
        input.insertAdjacentElement('afterend', error);
        input.focus();
    }

    function clearMessages() {
        messageContainer.innerHTML = '';
        form.querySelectorAll('.text-red-500').forEach(el => el.remove());
    }
});
