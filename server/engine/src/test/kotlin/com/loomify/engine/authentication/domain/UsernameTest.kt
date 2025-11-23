package com.loomify.engine.authentication.domain

import com.loomify.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

@UnitTest
internal class UsernameTest {

    @Test
    fun `should get null from blank username`() {
        assertThat(Username.of(" ")).isNull()
    }

    @Test
    fun `should get username from actual username`() {
        assertThat(Username.of("user")).isEqualTo(Username("user"))
    }

    @Test
    fun `should get username value`() {
        assertThat(Username("user").value).isEqualTo("user")
    }

    @Test
    fun `should get null from 2 char username`() {
        assertThat(Username.of("ab")).isNull()
    }

    @Test
    fun `should get null from 101 char username`() {
        assertThat(Username.of("a".repeat(101))).isNull()
    }

    @Test
    fun `should throw exception when username is blank`() {
        val instanceOf =
            assertThatThrownBy { Username(" ") }.isInstanceOf(IllegalArgumentException::class.java)
        instanceOf.hasMessage("Username cannot be blank")
    }

    @Test
    fun `should throw exception when username is less than 3 characters`() {
        val instanceOf =
            assertThatThrownBy { Username("ab") }.isInstanceOf(IllegalArgumentException::class.java)
        instanceOf.hasMessage("Username must be between 3 and 100 characters")
    }

    @Test
    fun `should throw exception when username is more than 100 characters`() {
        val instanceOf =
            assertThatThrownBy { Username("a".repeat(101)) }.isInstanceOf(IllegalArgumentException::class.java)
        instanceOf.hasMessage("Username must be between 3 and 100 characters")
    }
}
