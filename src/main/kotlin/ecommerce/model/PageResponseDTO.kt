package ecommerce.model

data class PageResponseDTO<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val sort: SortDTO?,
    val pageable: PageableDTO?,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean,
)

data class PageableDTO(
    val sort: SortDTO,
    val offset: Long,
    val pageNumber: Int,
    val pageSize: Int,
    val paged: Boolean,
    val unpaged: Boolean,
)

data class SortDTO(
    val empty: Boolean,
    val sorted: Boolean,
    val unsorted: Boolean,
)
