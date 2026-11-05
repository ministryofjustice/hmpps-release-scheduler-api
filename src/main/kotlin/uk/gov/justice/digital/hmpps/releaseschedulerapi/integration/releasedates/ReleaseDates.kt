package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.releasedates

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

data class ReleaseDates(@JsonAlias("prisonerNumber") val personIdentifier: String, val dates: List<ReleaseDate>)

data class ReleaseDate(val type: String, val date: LocalDate)
