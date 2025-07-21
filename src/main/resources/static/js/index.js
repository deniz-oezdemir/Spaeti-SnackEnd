async function getProduct(id) {
    const res = await fetch(`/api/products/${id}`);
    if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || 'Product not found');
    }
    return res.json();
}

async function updateProduct(id, data) {
    const res = await fetch(`/api/products/${id}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });
    if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || 'Failed to update product');
    }
    return res;
}

async function editProduct(id) {
    try {
        const product = await getProduct(id)
        document.getElementById('editProductId').value = id;
        document.getElementById('editProductName').value = product.name;
        document.getElementById('editProductPrice').value = product.price;
        document.getElementById('editProductImage').value = product.imageUrl;
        const modal = new bootstrap.Modal(document.getElementById('editProductModal'));
        modal.show();
    } catch (error) {
        showNotification(`Error loading product data.\n${error}`, 'error');
    }
}

document.getElementById('editProductForm').addEventListener('submit', (e) => editFormHandler(e));

async function editFormHandler(e) {
    e.preventDefault();
    const id = document.getElementById('editProductId').value;
    const name = document.getElementById('editProductName').value;
    const price = parseFloat(document.getElementById('editProductPrice').value);
    const imageUrl = document.getElementById('editProductImage').value;

    try {
        await updateProduct(id, {name, price, imageUrl});
        showNotification('Product updated successfully', 'success');
        location.reload();
    } catch (error) {
        console.error('Error updating product:', error);
        showNotification(`Error updating product: ${error.message}`, 'error');
    }
}

async function deleteProductById(id) {
    try {
        const res = await fetch(`/api/products/${id}`, {method: 'DELETE'});
        if (!res.ok) {
            const errorText = await res.text();
            showNotification(errorText || 'Failed to delete product', 'error');
            return;
        }
        const element = document.getElementById(id);
        if (element) {
            element.remove();
            showNotification("ok, deleted", 'success');
        } else {
            console.error(`Product element with ID ${id} not found in DOM.`);
        }
    } catch (error) {
        console.error(`Error while deleting product with id ${id}:`, error);
        showNotification(`Error: ${error.message}`, 'error');
    }
}

function toggleAllCheckboxes(checked) {
    const checkboxes = document.querySelectorAll('.form-check-input');
    checkboxes.forEach(checkbox => {
        checkbox.checked = checked;
    });
}

async function deleteSelectedProducts() {
    const selectedIds = Array.from(document.querySelectorAll('.form-check-input'))
        .filter(cb => cb.checked)
        .map(cb => cb.value);

    if (selectedIds.length === 0) {
        showNotification('No products selected.', 'error');
        return;
    }

    const confirmDelete = confirm(`Delete ${selectedIds.length} product(s)?`);
    if (!confirmDelete) return;

    try {
        for (const id of selectedIds) {
            const res = await fetch(`/api/products/${id}`, { method: 'DELETE' });
            if (!res.ok) {
                const errorText = await res.text();
                showNotification(`Error deleting product ${id}: ${errorText}`, 'error');
                continue;
            }
            document.getElementById(id)?.remove();
        }
        document.getElementById('selectAll').checked = false;
        showNotification(`${selectedIds.length} product(s) deleted`, 'success');
    } catch (err) {
        console.error(err);
        showNotification('Error deleting products', 'error');
    }
}

function showNotification(message, type) {
    const notification = document.createElement('div')
    notification.className = `alert alert-${type === 'success' ? 'success' : 'danger'} alert-dismissible`
    notification.innerHTML = `${message} <button type="button" class="btn-close" data-bs-dismiss="alert"></button>`
    const closeButton = notification.querySelector('.btn-close')
    closeButton.addEventListener('click', () => {
        notification.remove()
    })
    document.querySelector('.container').prepend(notification)
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove()
        }
    }, 1000)
}
