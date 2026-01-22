package com.cvix.common.domain.model.pagination

/**
 * Página basada en cursor para la capa de dominio/application.
 * Contiene la colección de elementos y cursores prev/next.
 */
data class CursorPage<T>(
    val data: Collection<T>,
    val prevPageCursor: String? = null,
    val nextPageCursor: String? = null,
) {
    /**
     * Transforma la colección completa usando una función que recibe la colección.
     * Mantiene los cursores.
     */
    inline fun <U> map(
        func: (Collection<T>) -> Collection<U>
    ): CursorPage<U> = CursorPage(
        data = func(data),
        prevPageCursor = prevPageCursor,
        nextPageCursor = nextPageCursor,
    )

    /**
     * Conveniencia para transformar cada elemento individualmente.
     */
    inline fun <U> mapItems(
        transform: (T) -> U
    ): CursorPage<U> = map { it.map(transform) }
}
