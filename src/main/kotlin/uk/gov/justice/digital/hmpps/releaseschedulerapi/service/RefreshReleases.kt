package uk.gov.justice.digital.hmpps.releaseschedulerapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.DomainEventPublisher
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.InternalEvents
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.InternalPrisonInformation
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.PersonReference
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.PrisonerUpdated
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.PrisonerUpdatedInformation
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.RefreshPrisonReleases
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.prisonregister.PrisonRegisterClient

@Service
class RefreshReleases(
  private val prisonRegister: PrisonRegisterClient,
  private val prisonerSearch: PrisonerSearchClient,
  private val dePublisher: DomainEventPublisher,
) {

  fun active() {
    prisonRegister.findAllPrisons()
      .map { RefreshPrisonReleases(InternalPrisonInformation(it.code)) }
      .also { dePublisher.handleInternalEvents(InternalEvents(it)) }
  }

  fun prison(event: RefreshPrisonReleases) {
    prisonerSearch.findPrisonersFor(event.additionalInformation.prisonCode)
      .map {
        PrisonerUpdated(
          PrisonerUpdatedInformation(it.prisonerNumber, setOf("PERSONAL_DETAILS", "SENTENCE")),
          PersonReference.withIdentifier(it.prisonerNumber),
        )
      }.also { dePublisher.handleInternalEvents(InternalEvents(it)) }
  }
}
