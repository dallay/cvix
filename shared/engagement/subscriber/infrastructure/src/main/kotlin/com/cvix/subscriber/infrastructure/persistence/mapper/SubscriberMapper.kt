package com.cvix.subscriber.infrastructure.persistence.mapper

import com.cvix.common.domain.model.Language
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.vo.email.Email
import com.cvix.common.domain.vo.ip.IpHash
import com.cvix.subscriber.domain.Subscriber
import com.cvix.subscriber.domain.SubscriberId
import com.cvix.subscriber.domain.SubscriptionSource
import com.cvix.subscriber.infrastructure.persistence.entity.SubscriberEntity

object SubscriberMapper {
    /**
     * Extension function to convert a Subscriber domain object to a SubscriberEntity persistence object.
     *
     * @return The SubscriberEntity object.
     */
    fun Subscriber.toEntity(): SubscriberEntity = SubscriberEntity(
        id = id.value,
        email = email.value,
        source = source.source,
        sourceRaw = sourceRaw,
        status = status,
        language = language.code,
        ipHash = ipHash?.value,
        attributes = attributes,
        confirmationToken = confirmationToken,
        confirmationExpiresAt = confirmationExpiresAt,
        doNotContact = doNotContact,
        workspaceId = workspaceId.value,
        createdAt = createdAt,
        createdBy = createdBy,
        updatedAt = updatedAt,
        updatedBy = updatedBy,
    )

    /**
     * Function to convert a SubscriberEntity persistence object to a Subscriber domain object.
     *
     * @return The Subscriber domain object.
     */

    fun SubscriberEntity.toDomain(): Subscriber = Subscriber(
        id = SubscriberId(id),
        email = Email(email),
        source = SubscriptionSource(source),
        sourceRaw = source,
        status = status,
        language = Language.fromString(language),
        ipHash = ipHash?.let { IpHash.from(it) },
        attributes = attributes,
        confirmationToken = confirmationToken,
        confirmationExpiresAt = confirmationExpiresAt,
        doNotContact = doNotContact,
        workspaceId = WorkspaceId(workspaceId),
        createdAt = createdAt,
        createdBy = createdBy,
        updatedAt = updatedAt,
        updatedBy = updatedBy,
    )
}
