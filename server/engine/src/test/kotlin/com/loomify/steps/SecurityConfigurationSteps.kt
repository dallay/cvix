package com.loomify.steps

import com.loomify.IntegrationTest
import com.loomify.engine.authentication.domain.Role
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThatCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@IntegrationTest
class SecurityConfigurationSteps {

    @Autowired
    private lateinit var grantedAuthoritiesMapper: GrantedAuthoritiesMapper

    private lateinit var authorities: MutableCollection<GrantedAuthority>

    @Given("a user with OidcUserAuthority")
    fun aUserWithOidcUserAuthority() {
        val claims: MutableMap<String, Any> = HashMap()
        claims["groups"] = listOf(Role.USER.key())
        claims["sub"] = 123
        claims["preferred_username"] = "admin"
        val idToken = OidcIdToken(OidcParameterNames.ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), claims)
        val userInfo = OidcUserInfo(claims)
        authorities = ArrayList()
        authorities.add(OidcUserAuthority(Role.USER.key(), idToken, userInfo))
    }

    @When("the authorities are mapped")
    fun theAuthoritiesAreMapped() {
        // No action needed here, the mapping is done in the 'then' step
    }

    @Then("the mapping should be successful")
    fun theMappingShouldBeSuccessful() {
        assertThatCode { grantedAuthoritiesMapper.mapAuthorities(authorities) }.doesNotThrowAnyException()
    }

    @Given("a user with SimpleGrantedAuthority")
    fun aUserWithSimpleGrantedAuthority() {
        authorities = ArrayList()
        authorities.add(SimpleGrantedAuthority(Role.USER.key()))
    }
}
