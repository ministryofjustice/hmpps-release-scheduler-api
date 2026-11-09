package uk.gov.justice.digital.hmpps.releaseschedulerapi.service

import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.SchedulerContext
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.ReleaseRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.getRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.GroupedReleaseDates
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.ReleaseDate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.ReleaseDetail
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.asPerson
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.referencedata.asCodedDescription
import java.util.UUID

@Transactional(readOnly = true)
@Service
class RetrieveReleases(private val releaseRepository: ReleaseRepository) {
  fun byId(id: UUID, checkCaseload: Boolean): ReleaseDetail = releaseRepository.getRelease(id)
    .takeIf { !checkCaseload || it.person.prisonCode == SchedulerContext.get().caseloadId }
    ?.detail()
    ?: throw AuthorizationDeniedException("Release not in caseload")

  private fun Release.detail() = ReleaseDetail(
    id = id,
    person.asPerson(),
    status.asCodedDescription(),
    reason?.asCodedDescription(),
    confirmedReleaseDate,
    autoReleaseDate,
    approvedReleaseDate,
    comments,
    groupedDates.calculatedDates().sortedByDescending { it.date },
  )

  private fun GroupedReleaseDates?.calculatedDates(): List<ReleaseDate<*>> = if (this == null) {
    emptyList()
  } else {
    approvedDates + autoDates
  }
}
