package uk.gov.justice.digital.hmpps.releaseschedulerapi.model

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.ReleaseDate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.referencedata.CodedDescription
import java.time.LocalDate
import java.util.UUID

data class ReleaseDetail(
  override val id: UUID,
  override val person: Person,
  override val status: CodedDescription,
  override val reason: CodedDescription?,
  override val confirmedReleaseDate: LocalDate?,
  override val autoReleaseDate: LocalDate?,
  override val approvedReleaseDate: LocalDate?,
  val comments: String?,
  val calculatedDates: List<ReleaseDate<*>>,
) : ReleaseInformation
