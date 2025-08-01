package ecommerce.model

data class PageResponseDTO<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val sort: Any?,
    val pageable: Any?,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean,
)
