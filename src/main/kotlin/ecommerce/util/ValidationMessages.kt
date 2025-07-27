package ecommerce.util

object ValidationMessages {
    const val NAME_REQUIRED = "Product name cannot be blank"
    const val PRICE_REQUIRED = "Price cannot be null"
    const val NAME_SIZE = "The product name must contain between 1 and 15 characters"
    const val NAME_PATTERN = "Invalid characters in product name."
    const val PRICE_POSITIVE = "Price must be greater than zero"
    const val IMAGE_REQUIRED = "Image URL cannot be blank"
    const val IMAGE_FORMAT = "Invalid imageUrl, should start with http:// or https://"
    const val EMAIL_BLANK = "Email cannot be blank"
    const val EMAIL_INVALID = "Email format is invalid"
    const val PASSWORD_BLANK = "Password cannot be blank"
    const val MEMBER_NAME_REQUIRED = "Name cannot be blank"
    const val QUANTITY_NON_NEGATIVE = "Quantity must be zero or a positive number"
}
