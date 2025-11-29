package com.loomify.authentication.application.query

import com.loomify.authentication.domain.UserSession
import com.loomify.authentication.domain.error.InvalidTokenException
import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.query.QueryHandler
import java.util.*
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder

@Service
class GetUserSessionQueryHandler(
    private val reactiveJwtDecoder: ReactiveJwtDecoder
) : QueryHandler<GetUserSessionQuery, UserSession> {

    override suspend fun handle(query: GetUserSessionQuery): UserSession = try {
        val jwt = reactiveJwtDecoder.decode(query.accessToken)?.awaitSingle()
            ?: throw InvalidTokenException("Invalid access token - decoder returned null")
        val userId = UUID.fromString(jwt.subject)
        val email = jwt.claims["email"] as? String
            ?: throw InvalidTokenException("Missing or invalid email claim in token")
        val roles = jwt.getClaimAsStringList("roles") ?: emptyList()
        UserSession(userId, email, roles)
    } catch (e: IllegalArgumentException) {
        throw InvalidTokenException("Invalid UUID format in token subject", e)
    } catch (e: ClassCastException) {
        throw InvalidTokenException("Invalid claim format in token", e)
    } catch (e: org.springframework.security.oauth2.jwt.JwtException) {
        throw InvalidTokenException("JWT decoding failed", e)
    } catch (e: Exception) {
        throw InvalidTokenException("Unexpected error during token processing", e)
    }
}
