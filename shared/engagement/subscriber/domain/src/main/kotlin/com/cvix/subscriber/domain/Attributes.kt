package com.cvix.subscriber.domain

data class Attributes(
    val tags: List<String>? = emptyList(),
    val metadata: Map<String, String>? = emptyMap()
)
