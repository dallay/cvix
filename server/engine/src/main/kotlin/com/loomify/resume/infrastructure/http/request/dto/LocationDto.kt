package com.loomify.resume.infrastructure.http.request.dto

import jakarta.validation.constraints.Size

/**
 * DTO for location information per JSON Resume Schema.
 */
data class LocationDto(
    @field:Size(max = 500, message = "Address must not exceed 500 characters")
    val address: String? = null,

    @field:Size(max = 20, message = "Postal code must not exceed 20 characters")
    val postalCode: String? = null,

    @field:Size(max = 100, message = "City must not exceed 100 characters")
    val city: String? = null,

    @field:Size(max = 2, message = "Country code must be 2 characters (ISO-3166-1 ALPHA-2)")
    val countryCode: String? = null,

    @field:Size(max = 100, message = "Region must not exceed 100 characters")
    val region: String? = null
)
