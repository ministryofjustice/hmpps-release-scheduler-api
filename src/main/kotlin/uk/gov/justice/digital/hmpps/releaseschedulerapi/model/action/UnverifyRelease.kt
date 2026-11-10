package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.RdProvider
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseUnverified
import uk.gov.justice.digital.hmpps.releaseschedulerapi.exception.ConflictException
import java.time.LocalDate

data class UnverifyRelease(
  override val confirmedReleaseDate: LocalDate,
  override val reasonCode: String,
  override val comments: String?,
) : ReleaseAction,
  ScheduleAction {
  companion object {
    private val VALID_STATUSES = setOf(ReleaseStatus.Code.VERIFIED.name, ReleaseStatus.Code.UNVERIFIED.name)
  }

  override fun applyTo(entity: Release, rdProvider: RdProvider) {
    if (entity.status.code !in VALID_STATUSES) {
      throw ConflictException("Cannot unverify from ${entity.status.code}")
    }
    entity.apply(this, rdProvider)
  }

  override fun changes(release: Release): Boolean = super changes release

  override fun domainEvent(entity: Release) = ReleaseUnverified(entity.person.identifier, entity.id)
}
