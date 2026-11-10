package uk.gov.justice.digital.hmpps.releaseschedulerapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.SchedulerContext
import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.set
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.ReleaseRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.getRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.AuditHistory
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.ReleaseActions
import uk.gov.justice.digital.hmpps.releaseschedulerapi.service.history.ReleaseHistoryService
import java.util.UUID

@Service
class ReleaseModifications(
  private val transactionTemplate: TransactionTemplate,
  private val rdRepository: ReferenceDataRepository,
  private val releaseRepository: ReleaseRepository,
  private val releaseHistory: ReleaseHistoryService,
) {
  fun apply(id: UUID, request: ReleaseActions): AuditHistory {
    SchedulerContext.get().copy(reason = request.reason).set()
    val (readVersion, writeVersion) = transactionTemplate.execute {
      val rdProvider = rdRepository.rdProvider()
      val release = releaseRepository.getRelease(id)
      val readVersion = release.version
      request.actions.forEach { it.applyTo(release, rdProvider) }
      releaseRepository.flush()
      readVersion!! to release.version!!
    }
    return AuditHistory(listOfNotNull(releaseHistory.currentAction(id, readVersion, writeVersion)))
  }
}
