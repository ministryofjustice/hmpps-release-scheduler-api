package uk.gov.justice.digital.hmpps.releaseschedulerapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceDataDomainRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.getDomain
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.referencedata.ReferenceDataResponse
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.referencedata.asCodedDescription

@Service
class ReferenceDataService(
  private val domainRepository: ReferenceDataDomainRepository,
  private val referenceDataRepository: ReferenceDataRepository,
) {
  fun findByDomain(code: ReferenceDataDomain.Code): ReferenceDataResponse {
    val domain = domainRepository.getDomain(code)
    val items = referenceDataRepository.findAllByType(code.clazz).filter { it.active }.sortedBy { it.sequenceNumber }
    return ReferenceDataResponse(domain.asCodedDescription(), items.map(ReferenceData::asCodedDescription))
  }
}
