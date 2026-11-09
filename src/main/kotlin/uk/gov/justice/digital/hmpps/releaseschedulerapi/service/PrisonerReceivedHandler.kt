package uk.gov.justice.digital.hmpps.releaseschedulerapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.ReleaseRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.PrisonerReceived
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.nomis.MigrationClient
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.prisonersearch.groupedDates
import java.time.LocalDateTime

@Service
class PrisonerReceivedHandler(
  private val personSummaryService: PersonSummaryService,
  private val rdRepository: ReferenceDataRepository,
  private val releaseRepository: ReleaseRepository,
  private val migrationClient: MigrationClient,
) {
  fun handle(pre: PrisonerReceived) {
    personSummaryService.getWithSave(pre.getPersonIdentifier()) { prisoner, person ->
      // TODO: remove when using CRD
      val rdProvider = rdRepository.rdProvider()
      releaseRepository.save(
        Release(
          person,
          rdProvider.get(ReleaseStatus.Code.PENDING.name),
          null,
          null,
          null,
          prisoner.groupedDates(),
          LocalDateTime.now(),
          null,
        ),
      )
    }

    // TODO: connect when available for booking switched
//    if (pre.additionalInformation.reason == BOOKING_SWITCHED_REASON) {
//       migrationClient.requestRepair(pre.additionalInformation.nomsNumber)
//    }
  }
}
