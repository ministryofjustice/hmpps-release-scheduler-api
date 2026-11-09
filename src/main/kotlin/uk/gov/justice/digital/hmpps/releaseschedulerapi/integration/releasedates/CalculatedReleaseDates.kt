package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.releasedates

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.ApprovedReleaseDate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.ApprovedReleaseType
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.AutoReleaseDate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.AutoReleaseType
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.GroupedReleaseDates
import java.time.LocalDate

data class CalculatedReleaseDates(
  @JsonAlias("prisonerNumber") val personIdentifier: String,
  @JsonSetter(contentNulls = Nulls.SKIP) val dates: List<InboundReleaseDate>,
)

data class InboundReleaseDate(val type: String, val date: LocalDate)

fun List<InboundReleaseDate>.asGroupedDates(): GroupedReleaseDates {
  val auto = mutableListOf<AutoReleaseDate>()
  val approved = mutableListOf<ApprovedReleaseDate>()
  for ((type, date) in this) {
    AutoReleaseType.from(type)?.let { auto += AutoReleaseDate(it, date) }
    ApprovedReleaseType.from(type)?.let { approved += ApprovedReleaseDate(it, date) }
  }
  return GroupedReleaseDates(auto, approved)
}
