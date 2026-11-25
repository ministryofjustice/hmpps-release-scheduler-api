package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.internal

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.SchedulerContext
import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.set
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.ReleaseRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.service.PersonSummaryService
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.ReferenceId
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.SyncReleaseRequest
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.asEntity
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.updateFrom

@Service
class ReleaseSync(
  private val transactionTemplate: TransactionTemplate,
  private val personSummaryService: PersonSummaryService,
  private val rdRepository: ReferenceDataRepository,
  private val releaseRepository: ReleaseRepository,
) {
  fun release(personIdentifier: String, request: SyncReleaseRequest): ReferenceId = with(request) {
    SchedulerContext.get().copy(
      requestAt = occurredAt,
      username = syncUser.username,
      caseloadId = syncUser.activeCaseloadId,
    ).set()

    val person = personSummaryService.getWithSave(personIdentifier)
    val saved: Release = transactionTemplate.execute {
      val rdProvider = rdRepository.rdProvider()
      (
        release.dpsId?.let { releaseRepository.findByIdOrNull(it) }
          ?: release.legacyBookingId?.let {
            releaseRepository.findByLegacyBookingIdAndLegacySequenceNumber(it, release.legacySequenceNumber)
          }
        )?.updateFrom(release, person, rdProvider)
        ?: releaseRepository.save(release.asEntity(person, occurredAt, rdProvider))
    }
    ReferenceId(saved.id)
  }
}
