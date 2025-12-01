package com.cvix.common.domain.presentation

import com.cvix.common.domain.bus.query.Response

open class PageResponse<T>(
    open val data: Collection<T>
) : Response
