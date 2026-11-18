package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync

import java.time.LocalDateTime

interface SyncRequest

interface NumericLegacyIdRequest : SyncRequest {
  val legacyId: Long?
}

interface StringLegacyIdRequest : SyncRequest {
  val legacyId: String?
}

data class SyncUser(val username: String, val activeCaseloadId: String?)

data class SyncReleaseRequest(
  val occurredAt: LocalDateTime,
  val syncUser: SyncUser,
  val release: ScheduledRelease,
)
