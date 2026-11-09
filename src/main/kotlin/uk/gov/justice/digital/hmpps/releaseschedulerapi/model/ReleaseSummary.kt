package uk.gov.justice.digital.hmpps.releaseschedulerapi.model

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.PersonSummary
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.referencedata.CodedDescription
import java.time.LocalDate
import java.util.UUID

data class ReleaseSummary(
  val id: UUID,
  val person: Person,
  val status: CodedDescription,
  val reason: CodedDescription?,
  val confirmedReleaseDate: LocalDate?,
  val autoReleaseDate: LocalDate?,
  val approvedReleaseDate: LocalDate?,
)

data class Person(val identifier: String, val firstName: String, val lastName: String)

fun PersonSummary.asPerson() = Person(identifier, firstName, lastName)
