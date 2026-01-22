package com.cvix.common.domain.model.pagination

/**
 * Página basada en offset para la capa de dominio/application.
 * Contiene la colección de elementos y metadatos de paginación.
 */
data class OffsetPage<T>(
    val data: Collection<T>,
    val total: Long? = null,
    val perPage: Int,
    val page: Int? = null,
    val totalPages: Int? = null,
) {
    /**
     * Transforma la colección completa usando una función que recibe la colección.
     * Mantiene los metadatos de paginación.
     */
    inline fun <U> map(
        func: (Collection<T>) -> Collection<U>
    ): OffsetPage<U> = OffsetPage(
        data = func(data),
        total = total,
        perPage = perPage,
        page = page,
        totalPages = totalPages,
    )

    /**
     * Conveniencia para transformar cada elemento individualmente.
     */
    inline fun <U> mapItems(
        transform: (T) -> U
    ): OffsetPage<U> = map { it.map(transform) }
}
