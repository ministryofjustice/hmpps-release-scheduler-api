package uk.gov.justice.digital.hmpps.releaseschedulerapi.service

import org.springframework.data.domain.Page
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.ReleaseRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.matchesPersonIdentifier
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.matchesPersonName
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.matchesPersonPrisonCode
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releaseDateBetween
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.statusCodeIn
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.ReleaseSummary
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.asPerson
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.referencedata.asCodedDescription
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.search.PageMetadata
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.search.ReleaseSearchRequest
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.search.ReleaseSearchResponse

@Transactional(readOnly = true)
@Service
class SearchReleases(private val releaseRepository: ReleaseRepository) {
  fun findForPrison(prisonCode: String, request: ReleaseSearchRequest): ReleaseSearchResponse = releaseRepository.findAll(request.asSpecification(prisonCode), request.pageable())
    .map { it.asSummary() }.asResponse()

  private fun ReleaseSearchRequest.asSpecification(prisonCode: String): Specification<Release> = (
    listOfNotNull(
      statusCodes.takeIf { it.isNotEmpty() }?.let { statusCodeIn(it) },
      if (query.isNullOrBlank()) releaseDateBetween(start!!, end!!) else null,
      query?.takeIf { it.isNotBlank() }?.let {
        if (it.isPersonIdentifier()) matchesPersonIdentifier(it, prisonCode) else matchesPersonName(it, prisonCode)
      } ?: matchesPersonPrisonCode(prisonCode),
    )
    ).reduce(Specification<Release>::and)

  private fun String.isPersonIdentifier(): Boolean = matches(Prisoner.PATTERN.toRegex())

  private fun Release.asSummary() = ReleaseSummary(
    id,
    person.asPerson(),
    status.asCodedDescription(),
    reason?.asCodedDescription(),
    confirmedReleaseDate,
    autoReleaseDate,
    approvedReleaseDate,
  )

  private fun Page<ReleaseSummary>.asResponse() = ReleaseSearchResponse(content, PageMetadata(totalElements))
}
