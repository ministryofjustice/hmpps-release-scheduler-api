package uk.gov.justice.digital.hmpps.releaseschedulerapi.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.PersonSummary
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.PersonSummaryRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.prisonersearch.PrisonerSearchClient

@Service
@Transactional
class PersonSummaryService(
  private val prisonerSearch: PrisonerSearchClient,
  private val personSummaryRepository: PersonSummaryRepository,
) {
  fun findPersonSummary(personIdentifier: String): PersonSummary? = personSummaryRepository.findByIdOrNull(personIdentifier)

  fun getWithSave(personIdentifier: String, code: (Prisoner, PersonSummary) -> Unit): PersonSummary = findPersonSummary(personIdentifier)
    ?: prisonerSearch.getPrisoner(personIdentifier)?.let {
      val person = personSummaryRepository.save(it.summary())
      code(it, person)
      person
    } ?: throw NotFoundException("Prisoner not found")

  fun updateExistingDetails(prisonNumber: String, code: (Prisoner, PersonSummary) -> Unit) {
    personSummaryRepository.findByIdOrNull(prisonNumber)?.also {
      val prisoner = requireNotNull(prisonerSearch.getPrisoner(prisonNumber)) { "Prisoner number invalid" }
      it.update(
        prisoner.firstName,
        prisoner.lastName,
        prisoner.responsiblePrison(),
      )
      code(prisoner, it)
    }
  }

  fun remove(personSummary: PersonSummary) = personSummaryRepository.delete(personSummary)

  private fun Prisoner.summary() = PersonSummary(firstName, lastName, responsiblePrison(), prisonerNumber)
}
