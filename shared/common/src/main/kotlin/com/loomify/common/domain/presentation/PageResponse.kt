package com.loomify.common.domain.presentation

import com.loomify.common.domain.bus.query.Response

open class PageResponse<T>(
    open val data: Collection<T>
) : Response
