package com.cvix.form.application

import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.FormBehaviorSettings
import com.cvix.form.domain.FormContentSettings
import com.cvix.form.domain.FormStylingSettings
import com.cvix.form.domain.HexColor
import com.cvix.form.domain.SubscriptionForm
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormSettings
import com.cvix.form.domain.SubscriptionFormStatus
import com.cvix.form.domain.SuccessActionType
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

    @Suppress("MagicNumber")
    fun randomSettings(): SubscriptionFormSettings {
        val actionType = SuccessActionType.entries.toTypedArray().random()
        val redirectUrl = if (actionType == SuccessActionType.REDIRECT) faker.internet().url() else null
        val successMessage = if (actionType == SuccessActionType.SHOW_MESSAGE) faker.lorem().sentence() else null

        return SubscriptionFormSettings(
            settings = FormBehaviorSettings(
                successActionType = actionType,
                successMessage = successMessage,
                redirectUrl = redirectUrl,
                confirmationRequired = Random.nextBoolean(),
            ),
            styling = FormStylingSettings(
                pageBackgroundColor = HexColor.from(randomHexColor()),
                backgroundColor = HexColor.from(randomHexColor()),
                textColor = HexColor.from(randomHexColor()),
                buttonColor = HexColor.from(randomHexColor()),
                buttonTextColor = HexColor.from(randomHexColor()),
                inputTextColor = HexColor.from(randomHexColor()),
                borderColor = HexColor.from(randomHexColor()),
                borderStyle = listOf("solid", "dashed", "dotted").random(),
                shadow = listOf("none", "small", "medium", "large").random(),
                borderThickness = Random.nextInt(0, 20),
                width = listOf("auto", "fit", "100%").random(),
                height = listOf("auto", "fit").random(),
                horizontalAlignment = listOf("left", "center", "right").random(),
                verticalAlignment = listOf("top", "center", "bottom").random(),
                padding = Random.nextInt(0, 50),
                gap = Random.nextInt(0, 50),
                cornerRadius = Random.nextInt(0, 50),
            ),
            content = FormContentSettings(
                showHeader = Random.nextBoolean(),
                showSubheader = Random.nextBoolean(),
                headerTitle = faker.lorem().sentence(),
                subheaderText = if (Random.nextBoolean()) faker.lorem().sentence() else null,
                inputPlaceholder = faker.internet().emailAddress(),
                submitButtonText = faker.lorem().word(),
                submittingButtonText = "${faker.lorem().word()}...",
                showTosCheckbox = Random.nextBoolean(),
                tosText = if (Random.nextBoolean()) faker.lorem().sentence() else null,
                showPrivacyCheckbox = Random.nextBoolean(),
                privacyText = if (Random.nextBoolean()) faker.lorem().sentence() else null,
            ),
        )
    }

    fun randomForm(
        id: SubscriptionFormId = SubscriptionFormId.random(),
        workspaceId: WorkspaceId = WorkspaceId.random(),
        status: SubscriptionFormStatus = SubscriptionFormStatus.PUBLISHED,
        name: String = faker.company().name(),
        description: String = faker.lorem().sentence(),
        createdBy: String = "test",
        createdAt: Instant = Instant.now(),
    ): SubscriptionForm {
        val settings = randomSettings()

        return when (status) {
            SubscriptionFormStatus.PUBLISHED -> SubscriptionForm.create(
                id = id,
                name = name,
                description = description,
                settings = settings,
                workspaceId = workspaceId,
                createdBy = createdBy,
                createdAt = createdAt,
            )

            SubscriptionFormStatus.DRAFT,
            SubscriptionFormStatus.DISABLED,
            SubscriptionFormStatus.ARCHIVED -> SubscriptionForm(
                id = id,
                name = name,
                description = description,
                settings = settings,
                status = status,
                workspaceId = workspaceId,
                createdAt = createdAt,
                createdBy = createdBy,
            )
        }
    }
}
