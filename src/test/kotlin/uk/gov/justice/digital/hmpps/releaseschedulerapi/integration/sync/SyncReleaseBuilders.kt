package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.sync

import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.newId
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.prisonCode
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.username
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.word
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.SyncRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.SyncReleaseRequest
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.SyncUser
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

fun syncUser(username: String = username(), activeCaseloadId: String? = prisonCode()) = SyncUser(username, activeCaseloadId)

fun syncRelease(
  legacyBookingId: Long? = newId(),
  legacySequenceNumber: Int? = newId().toInt(),
  dpsId: UUID? = null,
  releaseDate: LocalDate? = null,
  reasonCode: String? = null,
  statusCode: String? = SyncRelease.SCHEDULED,
  verified: Boolean = false,
  comments: String? = word(20),
  approvedReleaseDate: LocalDate? = null,
  autoReleaseDate: LocalDate? = null,
  dtoApprovedDate: LocalDate? = null,
  dtoMidTermDate: LocalDate? = null,
) = SyncRelease(
  legacyBookingId,
  legacySequenceNumber,
  dpsId,
  releaseDate,
  reasonCode,
  statusCode,
  verified,
  comments,
  approvedReleaseDate,
  autoReleaseDate,
  dtoApprovedDate,
  dtoMidTermDate,
)

fun syncReleaseRequest(
  syncRelease: SyncRelease = syncRelease(),
  syncUser: SyncUser = syncUser(),
  occurredAt: LocalDateTime = LocalDateTime.now(),
) = SyncReleaseRequest(
  release = syncRelease,
  syncUser = syncUser,
  occurredAt = occurredAt,
)

fun Release.syncRelease() = SyncRelease(
  legacyBookingId,
  legacySequenceNumber,
  id,
  confirmedReleaseDate,
  reason?.code,
  if (status.code == ReleaseStatus.Code.COMPLETED.name || confirmedReleaseDate?.isBefore(LocalDate.now()) == true) SyncRelease.EXPIRED else SyncRelease.SCHEDULED,
  status.code == ReleaseStatus.Code.VERIFIED.name,
  comments,
  approvedReleaseDate,
  autoReleaseDate,
  null,
  null,
)

infix fun Release.verifyAgainst(syncRelease: SyncRelease) {
  assertThat(confirmedReleaseDate).isEqualTo(syncRelease.releaseDate)
  assertThat(reason?.code).isEqualTo(syncRelease.releaseDate?.let { syncRelease.reasonCode })
  assertThat(comments).isEqualTo(syncRelease.comments)
  assertThat(autoReleaseDate).isEqualTo(syncRelease.autoReleaseDate)
  assertThat(approvedReleaseDate).isEqualTo(syncRelease.approvedReleaseDate)
}
