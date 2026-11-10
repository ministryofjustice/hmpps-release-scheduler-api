package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.RdProvider
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseVerified
import uk.gov.justice.digital.hmpps.releaseschedulerapi.exception.ConflictException

data object VerifyRelease : ReleaseAction {
  private val VALID_STATUSES = setOf(ReleaseStatus.Code.UNVERIFIED.name, ReleaseStatus.Code.VERIFIED.name)

  override fun applyTo(entity: Release, rdProvider: RdProvider) {
    if (entity.status.code !in VALID_STATUSES) {
      throw ConflictException("Cannot verify from ${entity.status.code}")
    }
    entity.apply(this, rdProvider)
  }

  override fun changes(release: Release): Boolean = release.status.code != ReleaseStatus.Code.VERIFIED.name

  override fun domainEvent(entity: Release) = ReleaseVerified(entity.person.identifier, entity.id)
}
