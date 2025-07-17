async function deleteProduct(id) {
    try {
        const response = await fetch(`api/products/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        })
        if (response.ok) {
            const element = document.getElementById(`${id}`)
            if (element) {
                element.remove()
                console.log("ok, deleted")
            } else {
                console.log("Not found", id)
            }
        } else {
            console.error("Product cannot be deleted")
        }
    } catch (error) {
        console.error("Error occurred:", error)
    }
}