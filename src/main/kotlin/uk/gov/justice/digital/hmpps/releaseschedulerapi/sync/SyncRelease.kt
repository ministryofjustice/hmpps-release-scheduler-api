package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import java.time.LocalDate
import java.util.UUID

@Schema(name = "SyncRelease")
data class SyncRelease(
  override val legacyBookingId: Long?,
  override val legacySequenceNumber: Int?,
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
) : SyncRequest {
  fun dpsStatusCode() = when (statusCode) {
    SCHEDULED if (releaseDate == null) -> ReleaseStatus.Code.PENDING
    SCHEDULED if (!releaseDate!!.isBefore(LocalDate.now())) -> if (verified) ReleaseStatus.Code.VERIFIED else ReleaseStatus.Code.UNVERIFIED
    else -> ReleaseStatus.Code.COMPLETED
  }

  companion object {
    const val SCHEDULED = "SCH"
    const val EXPIRED = "EXP"
  }
}
