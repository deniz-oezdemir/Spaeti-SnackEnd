package ecommerce.model

data class PageResponseDTO<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val sort: Any?, // or define your own SortResponse if needed
    val pageable: Any?, // or define PageableResponse
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)