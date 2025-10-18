package com.loomify.spring.boot.presentation.pagination

import com.loomify.common.domain.presentation.pagination.OffsetPageResponse
import com.loomify.spring.boot.presentation.ResponseBodyResultHandlerAdapter
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.reactive.accept.RequestedContentTypeResolver

@ControllerAdvice
class OffsetPageResponseHandler(
    serverCodecConfigurer: ServerCodecConfigurer,
    resolver: RequestedContentTypeResolver,
    presenter: OffsetPagePresenter
) : ResponseBodyResultHandlerAdapter<OffsetPageResponse<*>>(
    serverCodecConfigurer.writers,
    resolver,
    presenter,
)
