package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.internal

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.ResyncReleasesRequest
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.ResyncResponse

@Transactional
@Service
class ResyncReleases {
  fun all(personIdentifier: String, request: ResyncReleasesRequest): ResyncResponse {
    TODO()
  }
}
