package com.cvix.authentication.domain

import com.cvix.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@UnitTest
internal class RoleTest {
    @Test
    fun shouldGetRoleKey() {
        assertThat(Role.ADMIN.key()).isEqualTo("ROLE_ADMIN")
    }

    @Test
    fun shouldConvertUnknownRoleToUnknownRole() {
        assertThat(Role.from("ROLE_DUMMY")).isEqualTo(Role.UNKNOWN)
    }

    @Test
    fun shouldConvertFromRole() {
        assertThat(Role.from("ROLE_ADMIN")).isEqualTo(Role.ADMIN)
    }
}
