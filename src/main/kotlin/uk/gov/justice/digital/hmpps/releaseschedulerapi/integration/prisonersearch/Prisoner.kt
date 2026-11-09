package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.prisonersearch

import com.fasterxml.jackson.annotation.JsonIgnore
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.ApprovedReleaseDate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.ApprovedReleaseType
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.AutoReleaseDate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.AutoReleaseType
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.GroupedReleaseDates
import java.time.LocalDate

data class PrisonerNumbers(
  val prisonerNumbers: Set<String>,
)

data class Prisoner(
  val prisonerNumber: String,
  val firstName: String,
  val lastName: String,
  val status: String,
  val prisonId: String?,
  val lastPrisonId: String?,
  // temporary dates for testing using prisoner search
  val conditionalReleaseDate: LocalDate?,
  val automaticReleaseDate: LocalDate?,
  val nonParoleDate: LocalDate?,
  val postRecallReleaseDate: LocalDate?,
  val homeDetentionCurfewActualDate: LocalDate?,
  val actualParoleDate: LocalDate?,
) {
  @JsonIgnore
  fun responsiblePrison(): String = (if (status.startsWith(ACTIVE)) lastPrisonId else prisonId) ?: OUT

  companion object {
    private const val ACTIVE = "ACTIVE"
    private const val OUT = "OUT"

    fun fields() = arrayOf(
      Prisoner::prisonerNumber.name,
      Prisoner::firstName.name,
      Prisoner::lastName.name,
      Prisoner::status.name,
      Prisoner::prisonId.name,
      Prisoner::lastPrisonId.name,
      // temporary properties
      Prisoner::conditionalReleaseDate.name,
      Prisoner::automaticReleaseDate.name,
      Prisoner::nonParoleDate.name,
      Prisoner::postRecallReleaseDate.name,
      Prisoner::homeDetentionCurfewActualDate.name,
      Prisoner::actualParoleDate.name,
    )

    const val PATTERN: String = "\\w\\d{4}\\w{2}"
  }
}

fun Prisoner.groupedDates() = GroupedReleaseDates(
  listOfNotNull(
    conditionalReleaseDate?.let { AutoReleaseDate(AutoReleaseType.CRD, it) },
    automaticReleaseDate?.let { AutoReleaseDate(AutoReleaseType.ARD, it) },
    nonParoleDate?.let { AutoReleaseDate(AutoReleaseType.NPD, it) },
    postRecallReleaseDate?.let { AutoReleaseDate(AutoReleaseType.PRRD, it) },
  ),
  listOfNotNull(
    homeDetentionCurfewActualDate?.let { ApprovedReleaseDate(ApprovedReleaseType.HDCAD, it) },
    actualParoleDate?.let { ApprovedReleaseDate(ApprovedReleaseType.APD, it) },
    postRecallReleaseDate?.let { ApprovedReleaseDate(ApprovedReleaseType.PRRD, it) },
  ),
)
