package com.cvix.form.application.update

import com.cvix.common.domain.bus.command.Command
import java.util.*

/**
 * Command to update an existing subscriber form.
 */
data class UpdateSubscriberFormCommand(
    val id: UUID,
    val name: String,
    val header: String,
    val description: String,
    val inputPlaceholder: String,
    val buttonText: String,
    val buttonColor: String,
    val backgroundColor: String,
    val textColor: String,
    val buttonTextColor: String,
    val confirmationRequired: Boolean,
    val workspaceId: UUID,
    val userId: UUID,
) : Command
