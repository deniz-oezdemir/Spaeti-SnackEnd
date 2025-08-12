function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

document.addEventListener('DOMContentLoaded', () => {
    const fragment = document.getElementById("addProductModal")
    if (fragment && fragment.dataset.hasErrors === 'true') {
        const modal = new bootstrap.Modal(document.getElementById('addProductModal'));
        modal.show();
    }
});

async function getProduct(id) {
    const res = await fetch(`/api/products/${id}`);
    if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || 'Product not found');
    }
    return res.json();
}

async function updateProduct(id, data) {
    const token = getCookie('token');
    if (!token) {
        console.warn('No auth token cookie found, aborting getProduct');
        return;
    }
    const res = await fetch(`/api/products/${id}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`},
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
        document.getElementById('editProductImageUrl').value = product.imageUrl;
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
    const imageUrl = document.getElementById('editProductImageUrl').value;

    try {
        await updateProduct(id, {name, price, imageUrl});
        showNotification('Product updated successfully', 'success');
        location.reload();
    } catch (error) {
        let response;
        try {
            response = JSON.parse(error.message); // This is the full error object
        } catch {
            showNotification(`Unexpected error format: ${error.message}`, 'error');
            return;
        }

        const fieldErrors = response.message;
        if (fieldErrors && typeof fieldErrors === 'object') {
            for (const [field, message] of Object.entries(fieldErrors)) {
                const input = document.getElementById(`editProduct${capitalize(field)}`);
                if (input) {
                    input.classList.add('is-invalid');
                    const errorDiv = document.createElement('div');
                    errorDiv.className = 'text-danger small';
                    errorDiv.textContent = message;
                    input.parentElement.appendChild(errorDiv);
                } else {
                    console.warn(`No input found for field '${field}'`);
                }
            }
        } else {
            showNotification(`Error: ${response.error}`, 'error');
        }
    }
}

function clearFieldErrors() {
    document.querySelectorAll('#editProductForm .is-invalid').forEach(el => el.classList.remove('is-invalid'));
    document.querySelectorAll('#editProductForm .text-danger').forEach(el => el.remove());
}

function capitalize(s) {
    return s.charAt(0).toUpperCase() + s.slice(1);
}

async function deleteProductById(id) {
    const token = getCookie('token');
    if (!token) {
        console.warn('No auth token cookie found, aborting getProduct');
        return;
    }
    try {
        const res = await fetch(`/api/products/${id}`, {
            method: 'DELETE',
            headers: {'Authorization': `Bearer ${token}`}
        });
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
            const res = await fetch(`/api/products/${id}`, {method: 'DELETE'});
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
