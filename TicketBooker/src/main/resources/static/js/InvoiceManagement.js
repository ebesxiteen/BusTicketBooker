// file: searchInvoices.js

document.addEventListener('DOMContentLoaded', () => {
    // 1. Gọi tìm kiếm ngay khi vào trang (để hiện danh sách ban đầu)
    searchInvoices();

    // 2. Gắn sự kiện: Khi người dùng thay đổi bộ lọc -> Tự động tìm kiếm lại
    const inputs = ['totalAmount', 'paymentStatus', 'paymentMethod'];
    inputs.forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            // Dùng sự kiện 'input' cho ô nhập tiền (gõ đến đâu tìm đến đó)
            // Dùng sự kiện 'change' cho dropdown select
            const eventType = id === 'totalAmount' ? 'input' : 'change';
            
            // Debounce cho ô nhập tiền (tránh gọi API quá nhiều khi đang gõ)
            if (id === 'totalAmount') {
                let timeout;
                element.addEventListener('input', () => {
                    clearTimeout(timeout);
                    timeout = setTimeout(searchInvoices, 500); // Chờ 0.5s sau khi ngừng gõ mới tìm
                });
            } else {
                element.addEventListener('change', searchInvoices);
            }
        }
    });
});

async function searchInvoices() {
    const totalAmountInput = document.getElementById('totalAmount').value;
    const paymentStatusSelect = document.getElementById('paymentStatus').value;
    const paymentMethodSelect = document.getElementById('paymentMethod').value;

    const requestDTO = {
        totalAmount: totalAmountInput ? parseInt(totalAmountInput) : null,
        paymentStatus: paymentStatusSelect || null,
        paymentMethod: paymentMethodSelect || null
    };

    try {
        const response = await fetch('/api/invoices/search', { // Đảm bảo Backend có API này
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestDTO)
        });

        if (response.ok) {
            const responseDTO = await response.json();
            // Kiểm tra cấu trúc trả về (listInvoices hay danh sách trực tiếp)
            const dataList = responseDTO.listInvoices ? responseDTO.listInvoices : responseDTO;
            updateTable(dataList);
        } else {
            console.error('Error fetching data:', response.statusText);
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

function updateTable(invoices) {
    const tableBody = document.getElementById('invoiceTableBody');
    if (!tableBody) return;
    
    tableBody.innerHTML = ''; 

    if (!invoices || invoices.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-gray-500">Không tìm thấy hóa đơn nào</td></tr>`;
        return;
    }

    invoices.forEach(invoice => {
        // --- SỬA MÀU TÍM THÀNH MÀU XANH (hover:bg-emerald-50) ---
        const row = `
            <tr class="border-b border-gray-200 hover:bg-emerald-50 transition-transform duration-300 ease-in-out hover:shadow-md hover:scale-[1.01] cursor-pointer">
                <td class="py-3 px-6 text-gray-700 font-medium">#${invoice.id}</td>
                <td class="py-3 px-6 font-bold text-emerald-600">
                    ${new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(invoice.totalAmount)}
                </td>
                <td class="py-3 px-6">
                    ${getStatusBadge(invoice.paymentStatus)}
                </td>
                <td class="py-3 px-6 text-gray-600">
                    ${new Date(invoice.paymentTime).toLocaleString('vi-VN')}
                </td>
                <td class="py-3 px-6">
                    <span class="text-sm font-medium text-gray-600 bg-gray-100 px-2 py-1 rounded">
                        ${invoice.paymentMethod}
                    </span>
                </td>
            </tr>
        `;
        tableBody.insertAdjacentHTML('beforeend', row);
    });
}

// Hàm phụ để render Badge trạng thái cho đẹp
function getStatusBadge(status) {
    if (status === 'PAID') {
        return `<span class="bg-emerald-100 text-emerald-700 px-3 py-1 rounded-full text-xs font-bold border border-emerald-200">
                    <i class="fas fa-check-circle mr-1"></i>Đã thanh toán
                </span>`;
    } else if (status === 'PENDING') {
        return `<span class="bg-yellow-100 text-yellow-700 px-3 py-1 rounded-full text-xs font-bold border border-yellow-200">
                    <i class="fas fa-clock mr-1"></i>Chờ xử lý
                </span>`;
    } else {
        return `<span class="bg-gray-100 text-gray-600 px-3 py-1 rounded-full text-xs font-bold border border-gray-200">
                    ${status}
                </span>`;
    }
}