// file: InvoiceManagement.js

let currentPage = 0;
let totalPages = 1;
let pageSize = 10;

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
            if (eventType === 'input') {
                let timeout;
                element.addEventListener('input', () => {
                    clearTimeout(timeout);
                    timeout = setTimeout(() => searchInvoices(0), 500); // Chờ 0.5s sau khi ngừng gõ mới tìm
                });
            } else {
                element.addEventListener('change', () => searchInvoices(0));
            }
        }
    });

    const pageSizeSelect = document.getElementById('pageSize');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', () => {
            pageSize = parseInt(pageSizeSelect.value);
            searchInvoices(0);
        });
    }
});

async function searchInvoices(page = currentPage) {
    currentPage = page;
    const totalAmountInput = document.getElementById('totalAmount').value;
    const paymentStatusSelect = document.getElementById('paymentStatus').value;
    const paymentMethodSelect = document.getElementById('paymentMethod').value;
    const sizeSelect = document.getElementById('pageSize');
    pageSize = sizeSelect ? parseInt(sizeSelect.value) : pageSize;

    const requestDTO = {
        totalAmount: totalAmountInput ? parseInt(totalAmountInput) : null,
        paymentStatus: paymentStatusSelect || null,
        paymentMethod: paymentMethodSelect || null,
        page: currentPage,
        size: pageSize
    };

    try {
        const response = await fetch('/api/invoices/search', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestDTO)
        });

        if (response.ok) {
            const responseDTO = await response.json();
            const dataList = responseDTO.listInvoices ? responseDTO.listInvoices : responseDTO;
            totalPages = responseDTO.totalPages || 1;
            currentPage = responseDTO.currentPage || 0;
            updateTable(dataList);
            updatePaginationInfo();
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
        tableBody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-gray-500">Không tìm thấy hóa đơn nào</td></tr>`;
        return;
    }

    invoices.forEach(invoice => {
        const paymentTime = invoice.paymentTime ? new Date(invoice.paymentTime).toLocaleString('vi-VN') : '---';
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
                    ${paymentTime}
                </td>
                <td class="py-3 px-6">
                    <span class="text-sm font-medium text-gray-600 bg-gray-100 px-2 py-1 rounded">
                        ${invoice.paymentMethod}
                    </span>
                </td>
                <td class="py-3 px-6 text-center">
                    ${renderStatusUpdater(invoice)}
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

function renderStatusUpdater(invoice) {
    if (invoice.paymentStatus === 'CANCELLED') {
        return `<span class="text-sm text-gray-500">Không thể chỉnh sửa</span>`;
    }

    return `
        <select onchange="updatePaymentStatus(${invoice.id}, this.value)" class="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500">
            <option value="PAID" ${invoice.paymentStatus === 'PAID' ? 'selected' : ''}>Đã thanh toán</option>
            <option value="PENDING" ${invoice.paymentStatus === 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
        </select>
    `;
}

async function updatePaymentStatus(invoiceId, newStatus) {
    try {
        const response = await fetch(`/api/invoices/${invoiceId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ paymentStatus: newStatus })
        });

        if (!response.ok) {
            const errorText = await response.text();
            alert(errorText || 'Không thể cập nhật trạng thái');
            return;
        }

        searchInvoices(currentPage);
    } catch (error) {
        console.error('Error updating payment status:', error);
    }
}

function updatePaginationInfo() {
    const pageInfo = document.getElementById('pageInfo');
    const prevButton = document.getElementById('prevPage');
    const nextButton = document.getElementById('nextPage');

    if (pageInfo) {
        const total = totalPages || 1;
        pageInfo.textContent = `Trang ${totalPages === 0 ? 0 : currentPage + 1}/${total}`;
    }

    if (prevButton) {
        prevButton.disabled = currentPage <= 0;
        prevButton.classList.toggle('opacity-50', currentPage <= 0);
    }

    if (nextButton) {
        nextButton.disabled = totalPages === 0 || currentPage >= totalPages - 1;
        nextButton.classList.toggle('opacity-50', totalPages === 0 || currentPage >= totalPages - 1);
    }
}

function changePage(direction) {
    const targetPage = currentPage + direction;
    if (targetPage < 0 || targetPage >= totalPages) return;
    searchInvoices(targetPage);
}

