package uk.gov.justice.digital.hmpps.releaseschedulerapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.ReleaseRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.PrisonerUpdated
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.PrisonerUpdatedInformation.Companion.CATEGORIES_OF_INTEREST
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.prisonersearch.groupedDates
import java.time.LocalDateTime

@Service
class PersonUpdatedHandler(
  private val personSummaryService: PersonSummaryService,
  private val rdRepository: ReferenceDataRepository,
  private val releaseRepository: ReleaseRepository,
) {
  fun handle(de: PrisonerUpdated) {
    val matchingChanges = de.additionalInformation.categoriesChanged intersect CATEGORIES_OF_INTEREST
    if (matchingChanges.isNotEmpty()) {
      val rdProvider = rdRepository.rdProvider()
      personSummaryService.getWithSave(de.getPersonIdentifier()) { prisoner, person ->
        if (de.additionalInformation.categoriesChanged.contains("SENTENCE")) {
          releaseRepository.findByPersonIdentifierAndStatusCodeIn(de.getPersonIdentifier())
            ?.updateReleaseDates(prisoner.groupedDates(), rdProvider)
            ?: releaseRepository.save(
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
        person.update(prisoner.firstName, prisoner.lastName, prisoner.responsiblePrison())
      }
    }
  }
}
