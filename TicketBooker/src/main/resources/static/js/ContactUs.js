// Cấu hình Tailwind (nếu đang dùng CDN tailwindcss.com)
// Lưu ý: config này chỉ có hiệu lực nếu script này chạy TRƯỚC script CDN Tailwind.
// Nếu layout đã có tailwind.config riêng thì có thể bỏ đoạn này.
if (typeof tailwind !== "undefined") {
    tailwind.config = {
        theme: {
            extend: {
                colors: {
                    // Đổi sang bộ màu Xanh Lá GreenBus
                    primary: '#10b981',   // Emerald 500
                    secondary: '#047857', // Emerald 700
                    accent: '#d1fae5'     // Emerald 100
                }
            }
        }
    };
}

// Khởi tạo AOS sau khi DOM đã sẵn sàng
document.addEventListener('DOMContentLoaded', function () {
    if (typeof AOS !== 'undefined') {
        AOS.init({
            duration: 1000,           // thời gian animation
            easing: 'ease-out-cubic', // easing mượt hơn
            once: true                // chỉ animate 1 lần
        });
    } else {
        console.warn('AOS chưa được load – kiểm tra lại script AOS trong HTML.');
    }

    // ---- CHATBOX AI ----
    const chatForm = document.getElementById('chat-form');
    const chatInput = document.getElementById('chat-input');
    const chatMessages = document.getElementById('chat-messages');
    const chatStatus = document.getElementById('chat-status');

    if (!chatForm || !chatInput || !chatMessages) return;

    function escapeHtml(str) {
        return str
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    function appendMessage(sender, text) {
        const wrapper = document.createElement('div');
        wrapper.classList.add('flex', 'items-start', 'gap-2');

        const isUser = sender === 'user';

        if (isUser) {
            wrapper.classList.add('justify-end');
            wrapper.innerHTML = `
                <div class="bg-primary text-white rounded-2xl px-3 py-2 max-w-[80%] text-sm">
                    ${escapeHtml(text)}
                </div>
                <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center">
                    <i class="fas fa-user text-primary text-sm"></i>
                </div>
            `;
        } else {
            wrapper.innerHTML = `
                <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center">
                    <i class="fas fa-robot text-primary text-sm"></i>
                </div>
                <div class="bg-gray-100 rounded-2xl px-3 py-2 max-w-[80%]">
                    <p class="text-xs text-gray-500 mb-1">GreenBus AI</p>
                    <p class="text-sm text-gray-800 whitespace-pre-line">
                        ${escapeHtml(text)}
                    </p>
                </div>
            `;
        }

        chatMessages.appendChild(wrapper);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    chatForm.addEventListener('submit', async function (e) {
    e.preventDefault();
    const text = chatInput.value.trim();
    if (!text) return;

    appendMessage('user', text);
    chatInput.value = '';
    chatInput.focus();

    try {
        const response = await fetch(CHAT_API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain;charset=UTF-8'
            },
            body: text
        });

        const responseText = await response.text();
        appendMessage('bot', responseText || 'Không nhận được phản hồi từ AI.');
    } catch (err) {
        console.error(err);
        appendMessage('bot', 'Xin lỗi, hệ thống đang gặp sự cố. Bà thử lại sau giúp tui nha 🙏');
    }

    });
});