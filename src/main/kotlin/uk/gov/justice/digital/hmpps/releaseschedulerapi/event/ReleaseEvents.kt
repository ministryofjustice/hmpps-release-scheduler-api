package uk.gov.justice.digital.hmpps.releaseschedulerapi.event

import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.SchedulerContext
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.DataSource
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.IntegrationUrlBuilder.releaseUrl
import java.util.UUID

data class ReleaseInformation(override val id: UUID, override val source: DataSource) :
  IdInformation,
  SourceInformation

data class ReleaseMigrated(
  override val additionalInformation: ReleaseInformation,
  override val personReference: PersonReference,
) : DomainEvent<ReleaseInformation> {
  override val eventType: String = EVENT_TYPE
  override val description: String = DESCRIPTION
  override val detailUrl: String = releaseUrl(additionalInformation.id)

  companion object {
    const val EVENT_TYPE: String = "person.release.migrated"
    const val DESCRIPTION: String = "A release has been migrated"
    operator fun invoke(
      personIdentifier: String,
      id: UUID,
      dataSource: DataSource = SchedulerContext.get().source,
    ) = ReleaseMigrated(
      ReleaseInformation(id, dataSource),
      PersonReference.withIdentifier(personIdentifier),
    )
  }
}

data class ReleaseScheduled(
  override val additionalInformation: ReleaseInformation,
  override val personReference: PersonReference,
) : DomainEvent<ReleaseInformation> {
  override val eventType: String = EVENT_TYPE
  override val description: String = DESCRIPTION
  override val detailUrl: String = releaseUrl(additionalInformation.id)

  companion object {
    const val EVENT_TYPE: String = "person.release.scheduled"
    const val DESCRIPTION: String = "A release has been scheduled but remains unverified"
    operator fun invoke(
      personIdentifier: String,
      id: UUID,
      dataSource: DataSource = SchedulerContext.get().source,
    ) = ReleaseScheduled(
      ReleaseInformation(id, dataSource),
      PersonReference.withIdentifier(personIdentifier),
    )
  }
}

data class ReleaseUnscheduled(
  override val additionalInformation: ReleaseInformation,
  override val personReference: PersonReference,
) : DomainEvent<ReleaseInformation> {
  override val eventType: String = EVENT_TYPE
  override val description: String = DESCRIPTION
  override val detailUrl: String = releaseUrl(additionalInformation.id)

  companion object {
    const val EVENT_TYPE: String = "person.release.unscheduled"
    const val DESCRIPTION: String = "A previously scheduled release has been unscheduled"
    operator fun invoke(
      personIdentifier: String,
      id: UUID,
      dataSource: DataSource = SchedulerContext.get().source,
    ) = ReleaseUnscheduled(
      ReleaseInformation(id, dataSource),
      PersonReference.withIdentifier(personIdentifier),
    )
  }
}

data class ReleaseVerified(
  override val additionalInformation: ReleaseInformation,
  override val personReference: PersonReference,
) : DomainEvent<ReleaseInformation> {
  override val eventType: String = EVENT_TYPE
  override val description: String = DESCRIPTION
  override val detailUrl: String = releaseUrl(additionalInformation.id)

  companion object {
    const val EVENT_TYPE: String = "person.release.verified"
    const val DESCRIPTION: String = "A scheduled release has been verified"
    operator fun invoke(
      personIdentifier: String,
      id: UUID,
      dataSource: DataSource = SchedulerContext.get().source,
    ) = ReleaseVerified(
      ReleaseInformation(id, dataSource),
      PersonReference.withIdentifier(personIdentifier),
    )
  }
}

data class ReleaseUnverified(
  override val additionalInformation: ReleaseInformation,
  override val personReference: PersonReference,
) : DomainEvent<ReleaseInformation> {
  override val eventType: String = EVENT_TYPE
  override val description: String = DESCRIPTION
  override val detailUrl: String = releaseUrl(additionalInformation.id)

  companion object {
    const val EVENT_TYPE: String = "person.release.unverified"
    const val DESCRIPTION: String = "A scheduled release has been unverified"
    operator fun invoke(
      personIdentifier: String,
      id: UUID,
      dataSource: DataSource = SchedulerContext.get().source,
    ) = ReleaseUnverified(
      ReleaseInformation(id, dataSource),
      PersonReference.withIdentifier(personIdentifier),
    )
  }
}
