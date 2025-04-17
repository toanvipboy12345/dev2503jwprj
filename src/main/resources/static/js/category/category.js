(function() {
    $(document).ready(function() {
        // Sửa lỗi ARIA-hidden bằng cách quản lý focus
        const modal = $('#editCategoryModal');
        modal.on('hidden.bs.modal', function() {
            $('.edit-category-btn').first().focus();
            modal.removeAttr('aria-hidden'); // Xóa aria-hidden sau khi đóng để tránh cảnh báo
        });

        // Xử lý sự kiện click nút "Sửa"
        $('.edit-category-btn').on('click', function() {
            const id = $(this).data('id');
            const name = $(this).data('name');
            const slug = $(this).data('slug');
            const idParent = $(this).data('idparent');
            const isActive = $(this).data('isactive');
            const icon = $(this).data('icon');
            const metaTitle = $(this).data('metatitle');
            const metaKeyword = $(this).data('metakeyword');
            const metaDescription = $(this).data('metadescription');
            const notes = $(this).data('notes');
            const isDelete = $(this).data('isdelete');

            console.log('Dữ liệu từ nút Sửa:', {
                id, name, slug, idParent, isActive, icon, metaTitle, metaKeyword, metaDescription, notes, isDelete
            });

            $('#categoryId').val(id || '');
            $('#editName').val(name || '');
            $('#editSlug').val(slug || '');
            $('#editIdParent').val(idParent || '');
            $('#editIsActive').val(isActive);
            $('#editIsDelete').val(isDelete || '0');
            if (icon) {
                $('#currentIcon').removeClass('d-none');
                $('#currentIconImg').attr('src', icon);
            } else {
                $('#currentIcon').addClass('d-none');
            }
            $('#editMetaTitle').val(metaTitle || '');
            $('#editMetaKeyword').val(metaKeyword || '');
            $('#editMetaDescription').val(metaDescription || '');
            $('#editNotes').val(notes || '');

            $('#modal-error').addClass('d-none').text('');
        });

        // Xử lý sự kiện click nút "Cập Nhật"
        $('#saveCategoryBtn').on('click', function() {
            const form = $('#editCategoryForm')[0];
            const formData = new FormData(form);
            const categoryId = $('#categoryId').val();

            $.ajax({
                url: '/admin/categories/edit/' + categoryId,
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(response) {
                    console.log('Cập nhật danh mục thành công:', response);
                    console.log('URL ảnh:', response.icon);

                    // Đóng modal
                    $('#editCategoryModal').modal('hide');

                    // Hiển thị modal thành công
                    $('#successModal').modal('show');
                    setTimeout(() => $('#successModal').modal('hide'), 3000);

                    // Cập nhật hàng trong bảng danh mục
                    const row = $(`tr[data-id="${response.id}"]`);
                    if (row.length) {
                        // Cập nhật các cột
                        row.find('td:eq(1)').text(response.name || ''); // Tên
                        // Cập nhật cột Hình Ảnh
                        const imageCell = row.find('td:eq(2)');
                        if (response.icon) {
                            const timestamp = new Date().getTime();
                            imageCell.html('<span>Đang tải...</span>');
                            const img = new Image();
                            img.src = `${response.icon}?t=${timestamp}`;
                            img.onload = function() {
                                imageCell.html(`<img src="${response.icon}?t=${timestamp}" alt="Icon" style="width: 150px; object-fit: cover;" />`);
                            };
                            img.onerror = function() {
                                console.error('Không thể tải ảnh (lần 1):', response.icon);
                                setTimeout(() => {
                                    const retryImg = new Image();
                                    retryImg.src = `${response.icon}?t=${timestamp + 1}`;
                                    retryImg.onload = function() {
                                        imageCell.html(`<img src="${response.icon}?t=${timestamp + 1}" alt="Icon" style="width: 150px; object-fit: cover;" />`);
                                    };
                                    retryImg.onerror = function() {
                                        console.error('Không thể tải ảnh (lần 2):', response.icon);
                                        imageCell.html('Không có');
                                    };
                                }, 500);
                            };
                        } else {
                            imageCell.html('Không có');
                        }
                        row.find('td:eq(3)').text(response.slug || ''); // Slug
                        row.find('td:eq(4)').text(response.idParent ? response.idParent : 'Không có'); // Danh Mục Cha
                        row.find('td:eq(5)').text(response.isActive == 1 ? 'Hiển thị' : 'Ẩn'); // Trạng Thái
                        row.find('td:eq(6)').text(response.createdBy && window.customerNames && window.customerNames[response.createdBy] ? window.customerNames[response.createdBy] : 'Không xác định');
                        row.find('td:eq(7)').text(response.updatedBy && window.customerNames && window.customerNames[response.updatedBy] ? window.customerNames[response.updatedBy] : 'Không xác định');

                        // Cập nhật dữ liệu data-* của nút Sửa
                        const editButton = row.find('.edit-category-btn');
                        editButton.data('name', response.name);
                        editButton.data('slug', response.slug);
                        editButton.data('idparent', response.idParent);
                        editButton.data('isactive', response.isActive);
                        editButton.data('icon', response.icon);
                        editButton.data('metatitle', response.metaTitle);
                        editButton.data('metakeyword', response.metaKeyword);
                        editButton.data('metadescription', response.metaDescription);
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
                        errorMessage = errorObj.message || 'Có lỗi xảy ra khi cập nhật danh mục';
                    } catch (e) {
                        errorMessage = xhr.responseText || 'Có lỗi xảy ra khi cập nhật danh mục';
                    }
                    console.error('Lỗi khi cập nhật danh mục:', errorMessage);
                    $('#modal-error').removeClass('d-none').text(errorMessage);
                }
            });
        });

        // Xử lý sự kiện click nút xóa
        $('.delete-category-btn').on('click', function() {
            const categoryId = $(this).data('id');
            $('#confirmDeleteBtn').data('id', categoryId); // Lưu ID vào nút Xác nhận
        });

        // Xử lý sự kiện click nút Xác nhận xóa
        $('#confirmDeleteBtn').on('click', function() {
            const categoryId = $(this).data('id');
            $.ajax({
                url: '/admin/categories/delete/' + categoryId,
                type: 'POST',
                data: { _csrf: $('input[name="_csrf"]').val() },
                success: function(response) {
                    console.log('Xóa danh mục thành công:', response);
                    $('#deleteConfirmationModal').modal('hide');
                    $(`tr[data-id="${categoryId}"]`).remove(); // Xóa hàng khỏi bảng
                    $('#successModal').modal('show');
                    setTimeout(() => $('#successModal').modal('hide'), 3000);
                },
                error: function(xhr) {
                    let errorMessage;
                    try {
                        const errorObj = JSON.parse(xhr.responseText);
                        errorMessage = errorObj.message || 'Có lỗi xảy ra khi xóa danh mục';
                    } catch (e) {
                        errorMessage = xhr.responseText || 'Có lỗi xảy ra khi xóa danh mục';
                    }
                    console.error('Lỗi khi xóa danh mục:', errorMessage);
                    $('#modal-error').removeClass('d-none').text(errorMessage);
                }
            });
        });
    });
})();