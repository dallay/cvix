package com.cvix.form.application

import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.HexColor
import com.cvix.form.domain.SubscriptionForm
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormSettings
import com.cvix.form.domain.SubscriptionFormStatus
import java.time.Instant
import java.util.Locale
import kotlin.random.Random
import net.datafaker.Faker

object SubscriberFormStub {
    private val faker = Faker()
    private const val MAX_COLOR_VALUE = 0x1000000

    fun randomHexColor(): String {
        val value = Random.nextInt(MAX_COLOR_VALUE)
        return String.format(Locale.ROOT, "%06x", value)
    }

    fun randomSettings(): SubscriptionFormSettings = SubscriptionFormSettings(
        header = faker.lorem().sentence(),
        inputPlaceholder = faker.internet().emailAddress(),
        buttonText = faker.lorem().word(),
        buttonColor = HexColor.from(randomHexColor()),
        backgroundColor = HexColor.from(randomHexColor()),
        textColor = HexColor.from(randomHexColor()),
        buttonTextColor = HexColor.from(randomHexColor()),
    )

    fun randomForm(
        id: SubscriptionFormId = SubscriptionFormId.random(),
        workspaceId: WorkspaceId = WorkspaceId.random(),
        status: SubscriptionFormStatus = SubscriptionFormStatus.ACTIVE,
        name: String = faker.company().name(),
        description: String = faker.lorem().sentence(),
        createdBy: String = "test",
        createdAt: Instant = Instant.now(),
    ): SubscriptionForm {
        val settings = randomSettings()

        return when (status) {
            SubscriptionFormStatus.ACTIVE -> SubscriptionForm.create(
                id = id,
                name = name,
                description = description,
                settings = settings,
                workspaceId = workspaceId,
                createdBy = createdBy,
                createdAt = createdAt,
            )

            SubscriptionFormStatus.ARCHIVED -> SubscriptionForm(
                id = id,
                name = name,
                description = description,
                settings = settings,
                status = SubscriptionFormStatus.ARCHIVED,
                workspaceId = workspaceId,
                createdAt = createdAt,
                createdBy = createdBy,
            )
        }
    }
}
