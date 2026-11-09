package uk.gov.justice.digital.hmpps.releaseschedulerapi.model

import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.referencedata.CodedDescription
import java.time.LocalDate
import java.util.UUID

interface ReleaseInformation {
  val id: UUID
  val person: Person
  val status: CodedDescription
  val reason: CodedDescription?
  val confirmedReleaseDate: LocalDate?
  val autoReleaseDate: LocalDate?
  val approvedReleaseDate: LocalDate?
}
