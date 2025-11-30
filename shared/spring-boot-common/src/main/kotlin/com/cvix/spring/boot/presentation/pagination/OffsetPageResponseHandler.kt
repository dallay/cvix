package com.cvix.spring.boot.presentation.pagination

import com.cvix.common.domain.presentation.pagination.OffsetPageResponse
import com.cvix.spring.boot.presentation.ResponseBodyResultHandlerAdapter
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
