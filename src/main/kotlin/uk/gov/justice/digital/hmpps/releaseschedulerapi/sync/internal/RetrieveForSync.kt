package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.internal

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.ReleaseRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.getRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.ReconciliationRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.ReconciliationResponse
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.SyncRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.forSync
import java.util.UUID

@Transactional(readOnly = true)
@Service
class RetrieveForSync(private val releaseRepository: ReleaseRepository) {
  fun release(id: UUID): SyncRelease = releaseRepository.getRelease(id).forSync()

  fun all(personIdentifier: String): ReconciliationResponse = releaseRepository.findAllByPersonIdentifier(personIdentifier)
    .map { ReconciliationRelease(it.forSync()) }
    .let { ReconciliationResponse(it) }
}
