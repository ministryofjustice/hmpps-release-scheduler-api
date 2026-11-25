package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync

import java.time.LocalDateTime

interface SyncRequest {
  val legacyBookingId: Long?
  val legacySequenceNumber: Int?
}

data class SyncUser(val username: String, val activeCaseloadId: String?)

data class SyncReleaseRequest(
  val occurredAt: LocalDateTime,
  val syncUser: SyncUser,
  val release: SyncRelease,
)
