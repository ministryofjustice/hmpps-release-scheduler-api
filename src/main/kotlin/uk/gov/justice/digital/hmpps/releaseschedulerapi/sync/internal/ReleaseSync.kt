package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.internal

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.ReferenceId
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.SyncReleaseRequest

@Service
class ReleaseSync {
  fun release(personIdentifier: String, request: SyncReleaseRequest): ReferenceId {
    TODO()
  }
}
