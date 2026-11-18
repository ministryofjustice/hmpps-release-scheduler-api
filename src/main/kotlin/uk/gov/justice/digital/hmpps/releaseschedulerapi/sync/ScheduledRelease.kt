package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

@Schema(name = "SyncRelease")
data class ScheduledRelease(
  override val legacyId: Long?,
  val dpsId: UUID?,
  val releaseDate: LocalDate?,
  val reasonCode: String?,
  val statusCode: String?,
  val verified: Boolean,
  val comments: String?,
  val approvedReleaseDate: LocalDate?,
  val autoReleaseDate: LocalDate?,
  val dtoApprovedDate: LocalDate?,
  val dtoMidTermDate: LocalDate?,
) : NumericLegacyIdRequest
