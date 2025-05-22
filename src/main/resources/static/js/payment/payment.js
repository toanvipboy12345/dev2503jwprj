
console.log('transport.js file loaded');

(function() {
    console.log('jQuery loaded:', typeof $ !== 'undefined');

    $(document).ready(function() {
        console.log('Document ready - payment.js loaded');

        const modal = $('#editPaymentModal');
        modal.on('hidden.bs.modal', function() {
            $('.edit-payment-btn').first().focus();
            modal.removeAttr('aria-hidden');
        });

        $(document).on('click', '.edit-payment-btn', function() {
            const id = $(this).data('id');
            const name = $(this).data('name');
            const isActive = $(this).data('isactive');
            const notes = $(this).data('notes');
            const isDelete = $(this).data('isdelete');

            console.log('Dữ liệu từ nút Sửa:', { id, name, isActive, notes, isDelete });

            $('#paymentId').val(id || '');
            $('#editName').val(name || '');
            $('#editIsActive').val(isActive);
            $('#editIsDelete').val(isDelete || '0');
            $('#editNotes').val(notes || '');

            $('#modal-error').addClass('d-none').text('');
            $('#editPaymentModal').modal('show');
        });

        $('#savePaymentBtn').on('click', function() {
            const form = $('#editPaymentForm')[0];
            const formData = new FormData(form);
            const paymentId = $('#paymentId').val();

            $.ajax({
                url: '/admin/payment/edit/' + paymentId,
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(response) {
                    console.log('Cập nhật phương thức thanh toán thành công:', response);

                    $('#editPaymentModal').modal('hide');
                    $('#successModal').modal('show');
                    setTimeout(() => $('#successModal').modal('hide'), 3000);

                    const row = $(`tr[data-id="${response.id}"]`);
                    if (row.length) {
                        row.find('td:eq(1)').text(response.name || '');
                        row.find('td:eq(2)').text(response.isActive == 1 ? 'Hiển thị' : 'Ẩn');
                        row.find('td:eq(3)').text(response.createdDate ? new Date(response.createdDate).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : '');
                        row.find('td:eq(4)').text(response.updatedDate ? new Date(response.updatedDate).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : '');

                        const editButton = row.find('.edit-payment-btn');
                        editButton.data('name', response.name);
                        editButton.data('isactive', response.isActive);
                        editButton.data('notes', response.notes);
                        editButton.data('isdelete', response.isDelete);
                    } else {
                        console.error('Không tìm thấy hàng với ID:', response.id);
                    }
                },
                error: function(xhr) {
                    let errorMessage;
                    try {
                        const errorObj = JSON.parse(xhr.responseText);
                        errorMessage = errorObj.message || 'Có lỗi xảy ra khi cập nhật phương thức thanh toán';
                    } catch (e) {
                        errorMessage = xhr.responseText || 'Có lỗi xảy ra khi cập nhật phương thức thanh toán';
                    }
                    console.error('Lỗi khi cập nhật phương thức thanh toán:', errorMessage);
                    $('#modal-error').removeClass('d-none').text(errorMessage);
                }
            });
        });

        $(document).on('click', '.delete-payment-btn', function() {
            const paymentId = $(this).data('id');
            $('#confirmDeleteBtn').data('id', paymentId);
        });

        $('#confirmDeleteBtn').on('click', function() {
            const paymentId = $('#confirmDeleteBtn').data('id');
            $.ajax({
                url: '/admin/payment/delete/' + paymentId,
                type: 'POST',
                data: { _csrf: $('input[name="_csrf"]').val() },
                success: function(response) {
                    console.log('Xóa phương thức thanh toán thành công:', response);
                    $('#deleteConfirmationModal').modal('hide');
                    $(`tr[data-id="${paymentId}"]`).remove();
                    $('#successModal').modal('show');
                    setTimeout(() => $('#successModal').modal('hide'), 3000);
                },
                error: function(xhr) {
                    let errorMessage;
                    try {
                        const errorObj = JSON.parse(xhr.responseText);
                        errorMessage = errorObj.message || 'Có lỗi xảy ra khi xóa phương thức thanh toán';
                    } catch (e) {
                        errorMessage = xhr.responseText || 'Có lỗi xảy ra khi xóa phương thức thanh toán';
                    }
                    console.error('Lỗi khi xóa phương thức thanh toán:', errorMessage);
                    $('#modal-error').removeClass('d-none').text(errorMessage);
                }
            });
        });
    });
})();