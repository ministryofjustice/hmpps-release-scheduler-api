package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.RdProvider
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseCompleted

data object CompleteRelease : ReleaseAction {
  override fun applyTo(entity: Release, rdProvider: RdProvider) {
    entity.apply(this, rdProvider)
  }

  override fun changes(release: Release): Boolean = release.status.code != ReleaseStatus.Code.COMPLETED.name

  override fun domainEvent(entity: Release) = ReleaseCompleted(entity.person.identifier, entity.id)
}
