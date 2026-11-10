package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import java.time.LocalDate

interface ScheduleAction : CommentsAction {
  val confirmedReleaseDate: LocalDate
  val reasonCode: String

  infix fun changes(release: Release): Boolean = confirmedReleaseDate != release.confirmedReleaseDate ||
    reasonCode != release.reason?.code ||
    this changes release.comments
}
