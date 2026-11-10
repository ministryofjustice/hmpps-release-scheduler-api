package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.RdProvider
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseUnscheduled
import uk.gov.justice.digital.hmpps.releaseschedulerapi.exception.ConflictException

data object UnscheduleRelease : ReleaseAction {
  private val VALID_STATUSES = setOf(ReleaseStatus.Code.PENDING.name, ReleaseStatus.Code.UNVERIFIED.name, ReleaseStatus.Code.VERIFIED.name)

  override fun applyTo(entity: Release, rdProvider: RdProvider) {
    if (entity.status.code !in VALID_STATUSES) {
      throw ConflictException("Cannot unschedule from ${entity.status.code}")
    }
    entity.apply(this, rdProvider)
  }

  override fun changes(release: Release): Boolean = release.confirmedReleaseDate != null ||
    release.reason != null ||
    !release.comments.isNullOrBlank() ||
    release.status.code != ReleaseStatus.Code.PENDING.name

  override fun domainEvent(entity: Release) = ReleaseUnscheduled(entity.person.identifier, entity.id)
}
