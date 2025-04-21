(function() {
    $(document).ready(function() {
        const modal = $('#editProductModal');
        modal.on('hidden.bs.modal', function() {
            $('.edit-product-btn').first().focus();
            modal.removeAttr('aria-hidden');
        });

        $('.edit-product-btn').on('click', function() {
            const id = $(this).data('id');
            const product = window.productsData.find(p => p.id == id);
            if (!product) {
                console.error("Không tìm thấy sản phẩm với ID:", id);
                return;
            }

            const name = $(this).data('name');
            const slug = $(this).data('slug');
            const idCategory = $(this).data('idcategory');
            const categoryName = $(this).data('categoryname');
            const isActive = $(this).data('isactive');
            const image = $(this).data('image');
            const metaTitle = $(this).data('metatitle');
            const metaKeyword = $(this).data('metakeyword');
            const metaDescription = $(this).data('metadescription');
            const notes = $(this).data('notes');
            const description = $(this).data('description');
            const contents = $(this).data('contents');
            const price = $(this).data('price');
            const quantity = $(this).data('quantity');
            const isDelete = $(this).data('isdelete');
            const configurations = product.configurations || [];
            let images = product.images || [];

            console.log("Configurations data:", configurations);
            console.log("Images data:", images);

            $('#productId').val(id || '');
            $('#editName').val(name || '');
            $('#editSlug').val(slug || '');
            $('#editIdCategory').val(idCategory || '');
            $('#editCategoryName').val(categoryName || 'Không có');
            $('#editIsActive').val(isActive);
            $('#editIsDelete').val(isDelete || '0');
            if (image) {
                $('#currentImage').removeClass('d-none');
                $('#currentImageImg').attr('src', image);
            } else {
                $('#currentImage').addClass('d-none');
            }
            $('#editMetaTitle').val(metaTitle || '');
            $('#editMetaKeyword').val(metaKeyword || '');
            $('#editMetaDescription').val(metaDescription || '');
            $('#editNotes').val(notes || '');
            $('#editDescription').val(description || '');
            $('#editContents').val(contents || '');
            $('#editPrice').val(price || '');
            $('#editQuantity').val(quantity || '');

            $('#configContainer').empty();
            configurations.forEach((config, index) => {
                addConfigurationRow(config.configName, config.value, config.notes, index);
            });

            $('#imageContainer').empty();
            if (images && images.length > 0) {
                images.forEach((image, index) => {
                    addImageRow(image.id, image.urlImg, image.name, index, false);
                });
            } else {
                console.log("Không có ảnh phụ để hiển thị.");
            }

            $('#deleteImageIds').val('');

            console.log("Config container HTML:", $('#configContainer').html());
            console.log("Image container HTML:", $('#imageContainer').html());

            $('#editProductModal').modal('show');
        });

        function escapeHtml(unsafe) {
            if (unsafe == null) return '';
            return unsafe
                .toString()
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        }

        function addConfigurationRow(configName = '', value = '', notes = '', index) {
            const escapedConfigName = escapeHtml(configName);
            const escapedValue = escapeHtml(value);
            const escapedNotes = escapeHtml(notes);

            const row = `
                <div class="config-row mb-2 d-flex align-items-center">
                    <input type="text" name="configurations[${index}].configName" class="form-control me-2" value="${escapedConfigName}" placeholder="Tên cấu hình (VD: Phiên bản)" required>
                    <input type="text" name="configurations[${index}].value" class="form-control me-2" value="${escapedValue}" placeholder="Giá trị (VD: Bìa cứng)" required>
                    <input type="text" name="configurations[${index}].notes" class="form-control me-2" value="${escapedNotes}" placeholder="Ghi chú (tùy chọn)">
                    <button type="button" class="btn btn-danger btn-sm remove-config">Xóa</button>
                </div>`;
            $('#configContainer').append(row);
        }

        function addImageRow(id = '', urlImg = '', name = '', index, isNew = true) {
            const escapedId = escapeHtml(id);
            const escapedUrlImg = escapeHtml(urlImg);
            const escapedName = escapeHtml(name);

            const row = `
                <div class="image-row mb-2 d-flex align-items-center" data-id="${escapedId}">
                    ${isNew ? `
                        <input type="file" name="additionalImageFiles" class="form-control me-2" accept="image/jpeg,image/png,image/gif,image/webp" style="flex: 1;">
                    ` : `
                        <div class="d-flex align-items-center me-2" style="flex: 1;">
                            <img src="${escapedUrlImg}" alt="${escapedName}" style="width: 50px; height: 50px; object-fit: cover; margin-right: 10px;" />
                            <span>${escapedName}</span>
                        </div>
                    `}
                    <button type="button" class="btn btn-danger btn-sm remove-image">Xóa</button>
                </div>`;
            $('#imageContainer').append(row);
        }

        $('#addConfigBtn').on('click', function() {
            addConfigurationRow('', '', '', $('#configContainer .config-row').length);
        });

        $('#addImageBtn').on('click', function() {
            addImageRow('', '', '', $('#imageContainer .image-row').length, true);
        });

        $(document).on('click', '.remove-config', function() {
            $(this).closest('.config-row').remove();
            updateConfigIndices();
        });

        $(document).on('click', '.remove-image', function() {
            const row = $(this).closest('.image-row');
            const imageId = row.data('id');
            if (imageId) {
                let deleteImageIds = $('#deleteImageIds').val().split(',').filter(id => id);
                deleteImageIds.push(imageId);
                $('#deleteImageIds').val(deleteImageIds.join(','));
            }
            row.remove();
        });

        function updateConfigIndices() {
            $('#configContainer .config-row').each((index, row) => {
                $(row).find('input').each(function() {
                    const name = $(this).attr('name');
                    const newName = name.replace(/configurations\[\d+\]/, `configurations[${index}]`);
                    $(this).attr('name', newName);
                });
            });
        }

        function refreshRow(row, product) {
            // Tạo lại toàn bộ HTML của hàng trong bảng dựa trên dữ liệu mới
            const imageHtml = product.image ? 
                `<div><img src="${product.image}" alt="Image" style="width: 150px; object-fit: cover;" /></div>` : 
                `<span>Không có</span>`;
            const isActiveText = product.isActive == 1 ? 'Hiển thị' : 'Ẩn';
            const createdByName = product.createdBy && window.customerNames && window.customerNames[product.createdBy] ? 
                window.customerNames[product.createdBy] : 'Không xác định';
            const updatedByName = product.updatedBy && window.customerNames && window.customerNames[product.updatedBy] ? 
                window.customerNames[product.updatedBy] : 'Không xác định';

            row.html(`
                <td>${product.id}</td>
                <td>${escapeHtml(product.name)}</td>
                <td>${imageHtml}</td>
                <td>${escapeHtml(product.slug)}</td>
                <td>${escapeHtml(product.categoryName) || 'Không có'}</td>
                <td>${product.price || ''}</td>
                <td>${product.quantity || ''}</td>
                <td>${isActiveText}</td>
                <td>${createdByName}</td>
                <td>${updatedByName}</td>
                <td>
                    <button type="button" class="btn btn-warning btn-sm me-1 edit-product-btn"
                            data-id="${product.id}"
                            data-name="${escapeHtml(product.name)}"
                            data-slug="${escapeHtml(product.slug)}"
                            data-idcategory="${product.idCategory || ''}"
                            data-categoryname="${escapeHtml(product.categoryName) || 'Không có'}"
                            data-isactive="${product.isActive}"
                            data-image="${product.image || ''}"
                            data-metatitle="${escapeHtml(product.metaTitle)}"
                            data-metakeyword="${escapeHtml(product.metaKeyword)}"
                            data-metadescription="${escapeHtml(product.metaDescription)}"
                            data-notes="${escapeHtml(product.notes)}"
                            data-description="${escapeHtml(product.description)}"
                            data-contents="${escapeHtml(product.contents)}"
                            data-price="${product.price || ''}"
                            data-quantity="${product.quantity || ''}"
                            data-isdelete="${product.isDelete || '0'}"
                            data-bs-toggle="modal" data-bs-target="#editProductModal">
                        Sửa
                    </button>
                    <button type="button" class="btn btn-danger btn-sm delete-product-btn" 
                            data-id="${product.id}" 
                            data-bs-toggle="modal" 
                            data-bs-target="#deleteConfirmationModal">
                        Xóa
                    </button>
                </td>
            `);
        }

        $('#saveProductBtn').on('click', function() {
            const form = $('#editProductForm')[0];
            const formData = new FormData(form);
            const productId = $('#productId').val();

            $.ajax({
                url: '/admin/products/edit/' + productId,
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(response) {
                    console.log("Response từ server:", response); // Debug response

                    $('#editProductModal').modal('hide');
                    $('#successModal').modal('show');
                    setTimeout(() => $('#successModal').modal('hide'), 3000);

                    // Cập nhật window.productsData
                    const productIndex = window.productsData.findIndex(p => p.id == response.id);
                    if (productIndex !== -1) {
                        window.productsData[productIndex] = response;
                    } else {
                        window.productsData.push(response);
                    }

                    // Làm mới window.customerNames
                    $.ajax({
                        url: '/admin/products/customer-names',
                        type: 'GET',
                        success: function(customerNamesResponse) {
                            window.customerNames = customerNamesResponse;

                            // Cập nhật lại hàng trong bảng
                            const row = $(`tr[data-id="${response.id}"]`);
                            if (row.length) {
                                refreshRow(row, response);
                            } else {
                                console.error("Không tìm thấy hàng để cập nhật:", response.id);
                            }
                        },
                        error: function(xhr) {
                            console.error("Lỗi khi làm mới customerNames:", xhr.responseText);
                        }
                    });
                },
                error: function(xhr) {
                    let errorMessage;
                    try {
                        const errorObj = JSON.parse(xhr.responseText);
                        errorMessage = errorObj.message || 'Có lỗi xảy ra khi cập nhật sản phẩm';
                    } catch (e) {
                        errorMessage = xhr.responseText || 'Có lỗi xảy ra khi cập nhật sản phẩm';
                    }
                    $('#modal-error').removeClass('d-none').text(errorMessage);
                }
            });
        });

        $('.delete-product-btn').on('click', function() {
            const productId = $(this).data('id');
            $('#confirmDeleteBtn').data('id', productId);
        });

        $('#confirmDeleteBtn').on('click', function() {
            const productId = $(this).data('id');
            $.ajax({
                url: '/admin/products/delete/' + productId,
                type: 'POST',
                data: { _csrf: $('input[name="_csrf"]').val() },
                success: function() {
                    $('#deleteConfirmationModal').modal('hide');
                    $(`tr[data-id="${productId}"]`).remove();
                    $('#successModal').modal('show');
                    setTimeout(() => $('#successModal').modal('hide'), 3000);

                    window.productsData = window.productsData.filter(p => p.id != productId);
                },
                error: function(xhr) {
                    let errorMessage;
                    try {
                        const errorObj = JSON.parse(xhr.responseText);
                        errorMessage = errorObj.message || 'Có lỗi xảy ra khi xóa sản phẩm';
                    } catch (e) {
                        errorMessage = xhr.responseText || 'Có lỗi xảy ra khi xóa sản phẩm';
                    }
                    $('#modal-error').removeClass('d-none').text(errorMessage);
                }
            });
        });
    });
})();